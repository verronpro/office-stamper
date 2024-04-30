package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

import java.util.Objects;

/**
 * AbstractCommentProcessor is an abstract base class for comment processors.
 * It implements the CommentProcessor interface.
 * It provides common functionality and fields that subclasses can use.
 */
public abstract class AbstractCommentProcessor
        implements CommentProcessor {
    /**
     * PlaceholderReplacer used to replace expressions in the comment text.
     */
    protected final ParagraphPlaceholderReplacer placeholderReplacer;
    private P paragraph;
    private R currentRun;
    private Comment currentComment;
    private WordprocessingMLPackage document;

    /**
     * Creates an instance of AbstractCommentProcessor with the given ParagraphPlaceholderReplacer.
     *
     * @param placeholderReplacer the ParagraphPlaceholderReplacer used to replace expressions in the comment text
     */
    public AbstractCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        this.placeholderReplacer = placeholderReplacer;
    }

    /**
     * <p>Getter for the field <code>currentCommentWrapper</code>.</p>
     *
     * @return a {@link Comment} object
     */
    public Comment getCurrentCommentWrapper() {
        return currentComment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentCommentWrapper(Comment currentComment) {
        Objects.requireNonNull(currentComment.getCommentRangeStart());
        Objects.requireNonNull(currentComment.getCommentRangeEnd());
        this.currentComment = currentComment;
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
