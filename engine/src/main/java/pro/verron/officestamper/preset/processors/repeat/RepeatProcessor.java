package pro.verron.officestamper.preset.processors.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;
import org.jspecify.annotations.Nullable;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory.IRepeatProcessor;
import pro.verron.officestamper.utils.wml.WmlFactory;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;

public class RepeatProcessor
        extends CommentProcessor
        implements IRepeatProcessor {
    private static final Logger log = LoggerFactory.getLogger(RepeatProcessor.class);

    /// Constructs a new instance of CommentProcessor to process comments and placeholders within a paragraph.
    ///
    /// @param context the context containing the paragraph, comment, and placeholder associated with the
    ///         processing of this CommentProcessor.
    public RepeatProcessor(ProcessorContext context) {
        super(context);
    }

    private static SectPr getDocumentSection(ProcessorContext context) {
        try {
            return context.part()
                          .document()
                          .getMainDocumentPart()
                          .getContents()
                          .getBody()
                          .getSectPr();
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    @Override
    public void repeat(@Nullable Iterable<Object> items) {
        if (items == null) return;
        var comment = context().comment();
        var elements = comment.getElements();
        var contextHolder = context().contextHolder();
        var part = context().part();
        var parent = comment.getParent();
        var siblings = parent.getContent();
        var firstElement = elements.getFirst();
        var previousSectionBreak = previousSectionBreak(firstElement, parent).orElse(documentSection(context().part()));
        var index = siblings.indexOf(firstElement);
        siblings.removeAll(elements);
        var iterator = items.iterator();
        // Iterates items; copies elements; conditionally adds section break; adds elements
        while (iterator.hasNext()) {
            var item = iterator.next();
            var copiedElements = elements.stream()
                                         .map(XmlUtils::deepCopy)
                                         .collect(toCollection(ArrayList::new));
            WmlUtils.deleteCommentFromElements(comment.getId(), copiedElements);
            // Adds section break to last paragraph if needed
            if (iterator.hasNext() && containsSectionBreaks(copiedElements)) {
                var lastParagraph = lastParagraph(copiedElements).orElseGet(newEndParagraph(copiedElements));
                if (!hasSectionBreak(lastParagraph)) {
                    addSectionBreak(previousSectionBreak, lastParagraph);
                }
            }
            siblings.addAll(index, copiedElements);
            index += copiedElements.size();
            copiedElements.forEach(element -> {if (element instanceof Child child) child.setParent(parent);});
            var subContextKey = contextHolder.addBranch(item);
            Hooks.ofHooks(() -> copiedElements)
                 .forEachRemaining(hook -> hook.setContextKey(subContextKey));
        }
    }

    private static boolean containsSectionBreaks(ArrayList<Object> elements) {
        return elements.stream()
                       .filter(P.class::isInstance)
                       .map(P.class::cast)
                       .map(P::getPPr)
                       .filter(Objects::nonNull)
                       .map(PPr::getSectPr)
                       .anyMatch(Objects::nonNull);
    }

    private static Optional<P> lastParagraph(List<Object> elements) {
        if (elements.getLast() instanceof P paragraph) return Optional.of(paragraph);
        else return Optional.empty();
    }

    private static Supplier<P> newEndParagraph(ArrayList<Object> copiedElements) {
        return () -> {
            var p = WmlFactory.newParagraph();
            copiedElements.addLast(p);
            return p;
        };
    }

    private static boolean hasSectionBreak(P lastParagraph) {
        PPr pPr = lastParagraph.getPPr();
        if (pPr == null) return false;
        SectPr sectPr = pPr.getSectPr();
        return sectPr != null;
    }
}
