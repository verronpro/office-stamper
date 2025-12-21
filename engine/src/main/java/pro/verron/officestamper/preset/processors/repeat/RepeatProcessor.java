package pro.verron.officestamper.preset.processors.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;
import org.jspecify.annotations.Nullable;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.Hook;
import pro.verron.officestamper.core.SectionUtil;
import pro.verron.officestamper.preset.CommentProcessorFactory.IRepeatProcessor;
import pro.verron.officestamper.utils.wml.WmlFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;
import static pro.verron.officestamper.core.SectionUtil.addSectionBreak;

public class RepeatProcessor
        extends CommentProcessor
        implements IRepeatProcessor {

    /// Constructs a new instance of CommentProcessor to process comments and placeholders within a paragraph.
    ///
    /// @param context the context containing the paragraph, comment, and placeholder associated with the
    ///         processing of this CommentProcessor.
    public RepeatProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    public void repeat(@Nullable Iterable<Object> items) {
        if (items == null) return;
        var comment = context().comment();
        var elements = comment.getElements();
        var branch = context().branch();
        var part = context().part();
        var parent = comment.getParent();
        var siblings = parent.getContent();
        var firstElement = elements.getFirst();
        var previousSectionBreak = SectionUtil.getPreviousSectionBreakIfPresent(firstElement, parent)
                                              .orElse(RepeatProcessor.getDocumentSection(context()));
        var index = siblings.indexOf(firstElement);
        siblings.removeAll(elements);
        var iterator = items.iterator();
        while (iterator.hasNext()) {
            var item = iterator.next();
            var copiedElements = elements.stream()
                                         .map(XmlUtils::deepCopy)
                                         .collect(toCollection(ArrayList::new));
            CommentUtil.deleteCommentFromElements(comment, copiedElements);
            if (iterator.hasNext() && RepeatProcessor.containsSectionBreaks(copiedElements)) {
                var lastParagraph = RepeatProcessor.lastParagraph(copiedElements)
                                                   .orElseGet(RepeatProcessor.newEndParagraph(copiedElements));
                if (!RepeatProcessor.hasSectionBreak(lastParagraph)) {
                    addSectionBreak(previousSectionBreak, lastParagraph);
                }
            }
            siblings.addAll(index, copiedElements);
            index += copiedElements.size();
            copiedElements.forEach(element -> {if (element instanceof Child child) child.setParent(parent);});
            var subContextKey = branch.add(item);
            Hook.ofHooks(() -> copiedElements, part)
                .forEachRemaining(hook -> hook.setContextKey(subContextKey));
        }
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
