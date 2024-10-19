package pro.verron.officestamper.preset.processors.repeatparagraph;

import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.SectionUtil;
import pro.verron.officestamper.core.StandardParagraph;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.preset.Paragraphs;

import java.util.*;

import static pro.verron.officestamper.core.SectionUtil.getPreviousSectionBreakIfPresent;
import static pro.verron.officestamper.core.SectionUtil.hasOddNumberOfSectionBreaks;

/**
 * This class is used to repeat paragraphs and tables.
 * <p>
 * It is used internally by the DocxStamper and should not be instantiated by
 * clients.
 *
 * @author Joseph Verron
 * @author Youssouf Naciri
 * @version ${version}
 * @since 1.2.2
 */
public class ParagraphRepeatProcessor
        extends AbstractCommentProcessor
        implements CommentProcessorFactory.IParagraphRepeatProcessor {
    private Map<Paragraph, Paragraphs> pToRepeat = new HashMap<>();

    /**
     * @param placeholderReplacer replaces placeholders with values
     */
    private ParagraphRepeatProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    /**
     * <p>newInstance.</p>
     *
     * @param placeholderReplacer replaces expressions with values
     *
     * @return a new instance of ParagraphRepeatProcessor
     */
    public static CommentProcessor newInstance(ParagraphPlaceholderReplacer placeholderReplacer) {
        return new ParagraphRepeatProcessor(placeholderReplacer);
    }

    /**
     * {@inheritDoc}
     */
    @Override public void repeatParagraph(List<Object> objects) {
        var paragraph = getParagraph();
        var comment = getCurrentCommentWrapper();
        var data = new ArrayDeque<>(objects);
        var elements = comment.getElements();
        var previousSectionBreak = getPreviousSectionBreakIfPresent((Child) elements.get(0), comment.getParent());
        var oddNumberOfBreaks = hasOddNumberOfSectionBreaks(elements);
        var toRepeat = new Paragraphs(comment, data, elements, previousSectionBreak, oddNumberOfBreaks);
        pToRepeat.put(paragraph, toRepeat);
    }

    /**
     * {@inheritDoc}
     */
    @Override public void commitChanges(DocxPart document) {
        for (Map.Entry<Paragraph, Paragraphs> entry : pToRepeat.entrySet()) {
            Paragraph currentP = entry.getKey();
            ContentAccessor parent = (ContentAccessor) currentP.parent();
            List<Object> siblings = parent.getContent();
            int index = siblings.indexOf(currentP.getP());
            if (index < 0) throw new OfficeStamperException("Impossible");

            var toAdd = generateParagraphsToAdd(document, entry.getValue());

            siblings.addAll(index, toAdd.stream()
                                        .map(Paragraph::getP)
                                        .toList());
            siblings.removeAll(entry.getValue()
                                    .elements()
                                    .stream()
                                    .filter(P.class::isInstance)
                                    .map(P.class::cast)
                                    .toList());
        }
    }

    private Deque<Paragraph> generateParagraphsToAdd(
            DocxPart document,
            Paragraphs paragraphs
    ) {
        Deque<Paragraph> paragraphsToAdd = new ArrayDeque<>();
        var last = paragraphs.data()
                             .peekLast();
        for (Object expressionContext : paragraphs.data()) {
            for (Object paragraphToClone : paragraphs.elements()) {
                Object clone = XmlUtils.deepCopy(paragraphToClone);
                var comment = paragraphs.comment();
                var comment1 = comment.getComment();
                var commentId = comment1.getId();
                if (clone instanceof ContentAccessor contentAccessor) {
                    CommentUtil.deleteCommentFromElements(contentAccessor.getContent(), commentId);
                }
                if (clone instanceof P p) {
                    var paragraph = StandardParagraph.from(p);
                    placeholderReplacer.resolveExpressionsForParagraph(document, paragraph, expressionContext);
                    paragraphsToAdd.add(paragraph);
                }
            }
            var sectPr = paragraphs.previousSectionBreak();
            if (paragraphs.oddNumberOfBreaks() && sectPr.isPresent() && expressionContext != last) {
                SectionUtil.applySectionBreakToParagraph(sectPr.get(),
                        paragraphsToAdd.peekLast()
                                       .getP());

            }

        }
        return paragraphsToAdd;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void reset() {
        pToRepeat = new HashMap<>();
    }
}
