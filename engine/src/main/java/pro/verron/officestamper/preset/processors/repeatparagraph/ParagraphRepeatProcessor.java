package pro.verron.officestamper.preset.processors.repeatparagraph;

import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.SectionUtil;
import pro.verron.officestamper.core.StandardParagraph;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.preset.Paragraphs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyIterator;
import static pro.verron.officestamper.core.SectionUtil.getPreviousSectionBreakIfPresent;
import static pro.verron.officestamper.core.SectionUtil.hasOddNumberOfSectionBreaks;

/// Class used internally to repeat document elements.
/// Used by the lib, should not be instantiated by clients.
///
/// @author Joseph Verron
/// @author Youssouf Naciri
/// @version ${version}
/// @since 1.2.2
public class ParagraphRepeatProcessor
        extends AbstractCommentProcessor
        implements CommentProcessorFactory.IParagraphRepeatProcessor {

    /* TODO replace the mapping by a Paragraphs to List<Object> mapping to better reflect the change*/
    private Map<Paragraph, Paragraphs> pToRepeat = new HashMap<>();

    private ParagraphRepeatProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    /// Creates a new instance of [CommentProcessor] using the provided [ParagraphPlaceholderReplacer].
    ///
    /// @param placeholderReplacer the replacer to use for processing paragraph placeholders.
    ///
    /// @return a new instance of [ParagraphRepeatProcessor].
    public static CommentProcessor newInstance(ParagraphPlaceholderReplacer placeholderReplacer) {
        return new ParagraphRepeatProcessor(placeholderReplacer);
    }

    @Override public void repeatParagraph(Iterable<Object> objects) {
        var paragraph = getParagraph();
        var comment = getCurrentCommentWrapper();
        var elements = comment.getElements();
        var previousSectionBreak = getPreviousSectionBreakIfPresent(elements.getFirst(), comment.getParent());
        var oddNumberOfBreaks = hasOddNumberOfSectionBreaks(elements);
        var iterator = objects == null ? emptyIterator() : objects.iterator();
        var toRepeat = new Paragraphs(comment, iterator, elements, previousSectionBreak, oddNumberOfBreaks);
        pToRepeat.put(paragraph, toRepeat);
    }

    @Override public void commitChanges(DocxPart document) {
        for (Map.Entry<Paragraph, Paragraphs> entry : pToRepeat.entrySet()) {
            var current = entry.getKey();
            var replacement = entry.getValue();
            var toRemove = replacement.elements(P.class);
            var toAdd = generateParagraphsToAdd(document, replacement);
            current.replace(toRemove, toAdd);
        }
    }

    private List<P> generateParagraphsToAdd(DocxPart document, Paragraphs paragraphs) {
        var paragraphsToAdd = new LinkedList<P>();
        for (var it = paragraphs.data(); it.hasNext(); ) {
            Object expressionContext = it.next();
            for (Object paragraphToClone : paragraphs.elements()) {
                Object clone = XmlUtils.deepCopy(paragraphToClone);
                var comment = paragraphs.comment();
                if (clone instanceof ContentAccessor contentAccessor) {
                    CommentUtil.deleteCommentFromElements(comment, contentAccessor.getContent());
                }
                if (clone instanceof P p) {
                    var paragraph = StandardParagraph.from(document, p);
                    placeholderReplacer.resolveExpressionsForParagraph(document, paragraph, expressionContext);
                    paragraphsToAdd.add(p);
                }
            }
            var sectPr = paragraphs.previousSectionBreak();
            if (paragraphs.oddNumberOfBreaks() && sectPr.isPresent() && it.hasNext()) {
                assert paragraphsToAdd.peekLast() != null : "There should be at least one ";
                SectionUtil.applySectionBreakToParagraph(sectPr.get(), paragraphsToAdd.peekLast());
            }
        }
        return paragraphsToAdd;
    }

    @Override public void reset() {
        pToRepeat = new HashMap<>();
    }
}
