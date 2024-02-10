package pro.verron.docxstamper.commentProcessors;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.replace.PlaceholderReplacer;
import org.wickedsource.docxstamper.util.CommentWrapper;
import org.wickedsource.docxstamper.util.RunUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>CustomCommentProcessor class.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
public class CustomCommentProcessor extends BaseCommentProcessor implements ICustomCommentProcessor {

    private static final List<P> visitedParagraphs = new ArrayList<>();

    private P currentParagraph;

    /**
     * <p>Constructor for CustomCommentProcessor.</p>
     *
     * @param placeholderReplacer a {@link org.wickedsource.docxstamper.replace.PlaceholderReplacer} object
     */
    public CustomCommentProcessor(PlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    /**
     * <p>Getter for the field <code>visitedParagraphs</code>.</p>
     *
     * @return a {@link java.util.List} object
     */
    public static List<P> getVisitedParagraphs() {
        return visitedParagraphs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        visitedParagraphs.forEach(p -> {
            var content = p.getContent();
            content.clear();
            content.add(RunUtil.create("Visited."));
        });
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
    }

    /** {@inheritDoc} */
    @Override
    public void setCurrentRun(R run) {
    }

    /** {@inheritDoc} */
    @Override
    public void setParagraph(P paragraph) {
        currentParagraph = paragraph;
    }

    /** {@inheritDoc} */
    @Override
    public void setCurrentCommentWrapper(CommentWrapper commentWrapper) {
    }

    /** {@inheritDoc} */
    @Deprecated(since = "1.6.5", forRemoval = true)
    @Override
    public void setDocument(WordprocessingMLPackage document) {
    }

    /** {@inheritDoc} */
    @Override
    public void visitParagraph() {
        visitedParagraphs.add(currentParagraph);
    }
}
