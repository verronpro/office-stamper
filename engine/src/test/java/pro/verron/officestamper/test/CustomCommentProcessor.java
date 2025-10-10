package pro.verron.officestamper.test;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.*;

import java.util.ArrayList;
import java.util.List;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/**
 * This is an example of a custom {@link CommentProcessor} implementation.
 * <p>
 * Users of the docx-stamper library could use it to understand how they could
 * leverage the library to create their own custom comment processors.
 * <p>
 * Specifically, it's designed to replace each paragraph that has been
 * commented with the annotation "visitParagraph" exposed by the
 * {@link ICustomCommentProcessor#visitParagraph()} public method,
 * marking it with the text 'Visited' in the resultant stamped Word
 * document.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.6
 */
public class CustomCommentProcessor
        extends AbstractCommentProcessor
        implements ICustomCommentProcessor {

    private static final List<Paragraph> visitedParagraphs = new ArrayList<>();

    /**
     * <p>Constructor for CustomCommentProcessor.</p>
     *
     * @param placeholderReplacer a {@link ParagraphPlaceholderReplacer} object
     */
    public CustomCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitChanges(DocxPart document) {
        visitedParagraphs.forEach(para -> para.apply((P p)->{
            var content = p.getContent();
            content.clear();
            content.add(newRun("Visited"));
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        // DO NOTHING
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentCommentWrapper(Comment comment) {
        // DO NOTHING
    }

    @Override public void setParagraph(Paragraph paragraph) {
        super.setParagraph(paragraph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentRun(R run) {
        // DO NOTHING
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitParagraph() {
        visitedParagraphs.add(getParagraph());
    }
}
