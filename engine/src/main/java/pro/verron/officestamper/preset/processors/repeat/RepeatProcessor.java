package pro.verron.officestamper.preset.processors.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.Hooks;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory.IRepeatProcessor;
import pro.verron.officestamper.utils.wml.Content;
import pro.verron.officestamper.utils.wml.DocxDocument.Part;
import pro.verron.officestamper.utils.wml.WmlFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class RepeatProcessor extends CommentProcessor implements IRepeatProcessor {
    private static final Logger log = LoggerFactory.getLogger(RepeatProcessor.class);

    /// Constructs a new instance of CommentProcessor to process comments and placeholders within a paragraph.
    ///
    /// @param context the context containing the paragraph, comment, and placeholder associated with the
    ///         processing of this CommentProcessor.
    public RepeatProcessor(ProcessorContext context) {
        super(context);
    }

    private static Optional<SectPr> previousSectionBreak(Content.Element firstObject, Content content) {
        List<Content.Element> parentContent = content.getContent();
        int pIndex = parentContent.indexOf(firstObject);

        int i = pIndex - 1;
        while (i >= 0) {
            if (parentContent.get(i).get() instanceof P prevParagraph) {
                // the first P preceding the object is the one carrying a section break
                return ofNullable(prevParagraph.getPPr()).map(PPr::getSectPr);
            } else log.debug("The previous sibling was not a P, continuing search");
            i--;
        }
        log.info("No previous section break found from : {}, first object index={}", content, pIndex);
        return Optional.empty();
    }

    private static SectPr documentSection(Part part) {
        try {
            return part.document().getMainDocumentPart().getContents().getBody().getSectPr();
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private static boolean containsSectionBreaks(List<Content.Element> elements) {
        return elements.stream()
                .map(Content.Element::get)
                .filter(P.class::isInstance)
                .map(P.class::cast)
                .map(P::getPPr)
                .filter(Objects::nonNull)
                .map(PPr::getSectPr)
                .anyMatch(Objects::nonNull);
    }

    private static Optional<Content.Element> lastParagraph(List<Content.Element> elements) {
        if (elements.getLast().isParagraph()) return Optional.of(elements.getLast());
        else return Optional.empty();
    }

    private static boolean hasSectionBreak(P lastParagraph) {
        PPr pPr = lastParagraph.getPPr();
        if (pPr == null) return false;
        SectPr sectPr = pPr.getSectPr();
        return sectPr != null;
    }

    private static void addSectionBreak(SectPr sectPr, P paragraph) {
        PPr nextPPr = ofNullable(paragraph.getPPr()).orElseGet(WmlFactory::newPPr);
        nextPPr.setSectPr(XmlUtils.deepCopy(sectPr));
        paragraph.setPPr(nextPPr);
    }

    @Override
    public void repeat(@Nullable Iterable<Object> items) {
        if (items == null) return;
        var comment = context().comment();
        var contextHolder = context().contextHolder();
        var content = comment.getContent();
        var siblings = content.getContent();
        var firstElement = content.getFirst();
        var previousSectionBreak = previousSectionBreak(firstElement,
                content
        ).orElse(documentSection(context().part()));
        var index = siblings.indexOf(firstElement);
        content.clear();
        var iterator = items.iterator();
        // Iterates items; copies elements; conditionally adds section break; adds elements
        while (iterator.hasNext()) {
            var item = iterator.next();
            var copiedElements = content.copy();
            copiedElements.deleteCommentFromElements(comment.getId());
            // Adds section break to last paragraph if needed
            if (iterator.hasNext() && containsSectionBreaks(copiedElements.getContent())) {
                var lastParagraph = lastParagraph(copiedElements.getContent()).orElseGet(copiedElements::addEmptyParagraph);
                if (!hasSectionBreak((P) lastParagraph.get())) {
                    addSectionBreak(previousSectionBreak, (P) lastParagraph.get());
                }
            }
            content.add(index, copiedElements.getContent());
            index += copiedElements.size();
            var subContextKey = contextHolder.addBranch(item);
            Hooks.ofHooks(copiedElements).forEachRemaining(hook -> hook.setContextKey(subContextKey));
        }
    }
}
