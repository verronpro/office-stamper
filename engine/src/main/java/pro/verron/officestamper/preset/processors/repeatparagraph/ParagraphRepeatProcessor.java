package pro.verron.officestamper.preset.processors.repeatparagraph;

import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.SectionUtil;
import pro.verron.officestamper.core.StandardParagraph;
import pro.verron.officestamper.preset.Paragraphs;
import pro.verron.officestamper.preset.ProcessorFactory;

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
        extends AbstractProcessor
        implements ProcessorFactory.IParagraphRepeatProcessor {

    /* TODO replace the mapping by a Paragraphs to List<Object> mapping to better reflect the change*/
    private Map<Paragraph, Paragraphs> pToRepeat = new HashMap<>();

    private ParagraphRepeatProcessor() {

    }

    /// Creates a new instance of [Processor] using the provided [ParagraphPlaceholderReplacer].
    ///
    /// @return a new instance of [ParagraphRepeatProcessor].
    public static Processor newInstance() {
        return new ParagraphRepeatProcessor();
    }

    @Override
    public void repeatParagraph(Iterable<Object> objects) {
        var paragraph = getParagraph();
        var comment = getCurrentCommentWrapper();
        var elements = comment.getElements();
        var previousSectionBreak = getPreviousSectionBreakIfPresent(elements.getFirst(), comment.getParent());
        var oddNumberOfBreaks = hasOddNumberOfSectionBreaks(elements);
        var iterator = objects == null ? emptyIterator() : objects.iterator();
        var toRepeat = new Paragraphs(comment, iterator, elements, previousSectionBreak, oddNumberOfBreaks);
        pToRepeat.put(paragraph, toRepeat);
    }

    @Override
    public void commitChanges(DocxPart document) {
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
                var comment1 = comment.getComment();
                var commentId = comment1.getId();
                if (clone instanceof ContentAccessor contentAccessor) {
                    CommentUtil.deleteCommentFromElements(contentAccessor.getContent(), commentId);
                }
                if (clone instanceof P p) {
                    var paragraph = StandardParagraph.from(document, p);
                    paragraph.apply(para -> {
                        para.getContent()
                            .addFirst(new ContextSwitchAdd(expressionContext));
                        para.getContent()
                            .addLast(new ContextSwitchRem(expressionContext));
                    });
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

    @Override
    public void reset() {
        pToRepeat = new HashMap<>();
    }
}
