package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;

import java.util.Objects;

public abstract class AbstractCommentProcessor
        implements ICommentProcessor {
    /**
     * PlaceholderReplacer used to replace expressions in the comment text.
     */
    protected final ParagraphPlaceholderReplacer placeholderReplacer;
    private P paragraph;
    private R currentRun;
    private CommentWrapper currentCommentWrapper;
    private WordprocessingMLPackage document;

    public AbstractCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {this.placeholderReplacer = placeholderReplacer;}

    /**
     * <p>Getter for the field <code>currentCommentWrapper</code>.</p>
     *
     * @return a {@link CommentWrapper} object
     */
    public CommentWrapper getCurrentCommentWrapper() {
        return currentCommentWrapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
        Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
        Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
        this.currentCommentWrapper = currentCommentWrapper;
    }

    /**
     * <p>Getter for the field <code>paragraph</code>.</p>
     *
     * @return a {@link P} object
     */
    public P getParagraph() {
        return paragraph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParagraph(P paragraph) {
        this.paragraph = paragraph;
    }

    /**
     * <p>Getter for the field <code>currentRun</code>.</p>
     *
     * @return a {@link R} object
     */
    public R getCurrentRun() {
        return currentRun;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentRun(R run) {
        this.currentRun = run;
    }

    /**
     * <p>Getter for the field <code>document</code>.</p>
     *
     * @return a {@link WordprocessingMLPackage} object
     * @deprecated the document is passed to the processor through the commitChange method now
     * and will probably pe passed through the constructor in the future
     */
    @Deprecated(since = "1.6.5", forRemoval = true)
    public WordprocessingMLPackage getDocument() {
        return document;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated the document is passed to the processor through the commitChange method now,
     * and will probably pe passed through the constructor in the future
     */
    @Deprecated(since = "1.6.5", forRemoval = true)
    @Override
    public void setDocument(WordprocessingMLPackage document) {
        this.document = document;
    }
}
