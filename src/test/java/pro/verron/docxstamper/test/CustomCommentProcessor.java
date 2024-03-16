package pro.verron.docxstamper.test;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import pro.verron.docxstamper.api.AbstractCommentProcessor;
import pro.verron.docxstamper.api.Comment;
import pro.verron.docxstamper.api.CommentProcessor;
import pro.verron.docxstamper.api.ParagraphPlaceholderReplacer;

import java.util.ArrayList;
import java.util.List;

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

    private static final List<P> visitedParagraphs = new ArrayList<>();

    private P currentParagraph;

    /**
     * <p>Constructor for CustomCommentProcessor.</p>
     *
     * @param placeholderReplacer a {@link ParagraphPlaceholderReplacer} object
     */
    public CustomCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    public static R create(String string) {
        var factory = Context.getWmlObjectFactory();

        var text = factory.createText();
        text.setValue(string);

        var run = factory.createR();
        var runContent = run.getContent();
        runContent.add(text);

        return run;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        visitedParagraphs.forEach(p -> {
            var content = p.getContent();
            content.clear();
            content.add(create("Visited"));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentRun(R run) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParagraph(P paragraph) {
        currentParagraph = paragraph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentCommentWrapper(Comment comment) {
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated(since = "1.6.5", forRemoval = true)
    @Override
    public void setDocument(WordprocessingMLPackage document) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitParagraph() {
        visitedParagraphs.add(currentParagraph);
    }
}
