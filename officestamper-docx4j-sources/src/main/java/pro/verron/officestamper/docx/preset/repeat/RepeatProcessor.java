package pro.verron.officestamper.docx.preset.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.docx.JaxbCompat;
import pro.verron.officestamper.api.AnchoredComment;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.Element;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.docx.DocxDocumentUtil;
import pro.verron.officestamper.docx.DocxElement;
import pro.verron.officestamper.preset.CommentProcessorFactory.IRepeatProcessor;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.CommentProcessorContext;
import pro.verron.officestamper.docx.DocxPart;
import pro.verron.officestamper.docx.wml.WmlFactory;
import pro.verron.officestamper.docx.wml.WmlUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;

/// Repeats a series of document elements for each item of an iterable.
///
/// @author Joseph Verron
/// @author Youssouf Naciri
/// @since 1.2.2
public class RepeatProcessor
        extends CommentProcessor
        implements IRepeatProcessor {
    private static final Logger log = LoggerFactory.getLogger(RepeatProcessor.class);

    /// Constructs a new instance of CommentProcessor to process comments and placeholders within a paragraph.
    ///
    /// @param context the context containing the paragraph, comment, and placeholder associated with the
    ///         processing of this CommentProcessor.
    public RepeatProcessor(CommentProcessorContext context) {
        super(context);
    }

    @Override
    public void repeat(@Nullable Iterable<Object> items) {
        if (items == null) return;
        var comment = context().comment();
        var elements = comment.elements();
        if (log.isTraceEnabled()) {
            var region = elements.stream()
                                 .map(e -> DocxElement.rawOf(e)
                                                      .getClass()
                                                      .getSimpleName())
                                 .toList();
            log.trace("Repeat region: {}", region);
        }
        var contextHolder = context().contextHolder();
        var firstRaw = DocxElement.rawOf(elements.getFirst());
        var lastRaw = DocxElement.rawOf(elements.getLast());
        var parent = DocxDocumentUtil.findSmallestCommonParent(firstRaw, lastRaw);
        var siblings = parent.getContent();
        var previousSectionBreak = previousSectionBreak(firstRaw, parent)
                .orElse(documentSection(context().part()));
        var index = siblings.indexOf(firstRaw);
        siblings.removeAll(elements.stream()
                                   .map(DocxElement::rawOf)
                                   .toList());
        var iterator = items.iterator();
        // Iterates items; copies elements; conditionally adds section break; adds elements
        while (iterator.hasNext()) {
            var item = iterator.next();
            var copiedElements = copyElements(comment, elements);
            addSectionBreakForRepeatedElements(iterator, previousSectionBreak, copiedElements);
            siblings.addAll(index, copiedElements);
            index += copiedElements.size();
            copiedElements.forEach(element -> {
                JaxbCompat.setParent(element, parent);
            });
            var subContextKey = contextHolder.addBranch(item);
            // Every hook tag inside the copied elements (at any depth — runs live inside
            // paragraphs, cells, etc.) carries the sub-context key of this iteration
            // (legacy Hooks.ofHooks(copiedElements) semantics).
            attachSubContextKeys(copiedElements, subContextKey);
        }
    }

    /// Copies the repeated region's elements, stripping the anchor comment from the copies.
    private ArrayList<Object> copyElements(AnchoredComment comment, List<Element> elements) {
        var copiedElements = elements.stream()
                                     .map(DocxElement::rawOf)
                                     .map(XmlUtils::deepCopy)
                                     .collect(toCollection(ArrayList::new));
        WmlUtils.deleteCommentFromElements(new BigInteger(comment.id()), copiedElements);
        return copiedElements;
    }

    /// Adds a section break to the last copied paragraph when the original region carries one and
    /// another iteration follows.
    private void addSectionBreakForRepeatedElements(
            Iterator<Object> iterator,
            SectPr previousSectionBreak,
            ArrayList<Object> copiedElements
    ) {
        if (!iterator.hasNext() || !containsSectionBreaks(copiedElements)) return;
        var lastParagraph = lastParagraph(copiedElements).orElseGet(newEndParagraph(copiedElements));
        if (!hasSectionBreak(lastParagraph)) {
            addSectionBreak(previousSectionBreak, lastParagraph);
        }
    }

    /// Tags every hook found in the copied elements with the sub-context key of this iteration.
    private void attachSubContextKeys(List<Object> copiedElements, String subContextKey) {
        var discovery = context().part()
                                 .hooks();
        var part = (DocxPart) context().part();
        for (var copied : copiedElements) {
            var hooks = discovery.ofHooks(new DocxElement(copied, part));
            int found = 0;
            while (hooks.hasNext()) {
                hooks.next()
                     .setAttribute("context", subContextKey);
                found++;
            }
            if (log.isTraceEnabled()) log.trace("Repeat scan: element {} -> {} hooks keyed {}",
                    copied.getClass()
                          .getSimpleName(), found, subContextKey);
        }
    }

    private static Optional<SectPr> previousSectionBreak(Object firstObject, ContentAccessor parent) {
        List<Object> parentContent = parent.getContent();
        int pIndex = parentContent.indexOf(firstObject);

        int i = pIndex - 1;
        while (i >= 0) {
            if (parentContent.get(i) instanceof P prevParagraph) {
                // the first P preceding the object is the one carrying a section break
                return ofNullable(prevParagraph.getPPr()).map(PPr::getSectPr);
            }
            else log.debug("The previous sibling was not a P, continuing search");
            i--;
        }
        log.info("No previous section break found from : {}, first object index={}", parent, pIndex);
        return Optional.empty();
    }

    private static SectPr documentSection(OfficePart part) {
        try {
            return part.document()
                       .unwrap(WordprocessingMLPackage.class)
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

    private static void addSectionBreak(SectPr sectPr, P paragraph) {
        PPr nextPPr = ofNullable(paragraph.getPPr()).orElseGet(WmlFactory::newPPr);
        nextPPr.setSectPr(XmlUtils.deepCopy(sectPr));
        paragraph.setPPr(nextPPr);
    }
}
