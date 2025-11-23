package pro.verron.officestamper.api;

import java.util.Objects;

/// AbstractCommentProcessor is an abstract base class for comment processors.
/// It implements the CommentProcessor interface.
/// It provides common functionality and fields that subclasses can use.
public abstract class AbstractCommentProcessor
        implements CommentProcessor {

    /// PlaceholderReplacer used to replace expressions in the comment text.
    protected final ParagraphPlaceholderReplacer placeholderReplacer;
    private Paragraph paragraph;
    private Comment currentComment;

    /// Creates an instance of AbstractCommentProcessor with the given ParagraphPlaceholderReplacer.
    ///
    /// @param placeholderReplacer the ParagraphPlaceholderReplacer used to replace expressions in the comment text
    protected AbstractCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        this.placeholderReplacer = placeholderReplacer;
    }

    /// Retrieves the current comment wrapper associated with the processor.
    ///
    /// @return the current [Comment] object being processed
    public Comment getCurrentCommentWrapper() {
        return currentComment;
    }

    @Override
    public void setCurrentCommentWrapper(Comment currentComment) {
        Objects.requireNonNull(currentComment.getCommentRangeStart());
        Objects.requireNonNull(currentComment.getCommentRangeEnd());
        this.currentComment = currentComment;
    }

    @Override
    public void setProcessorContext(ProcessorContext processorContext) {
        setParagraph(processorContext.paragraph());
        setCurrentCommentWrapper(processorContext.comment());
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

    /// Sets the current paragraph being processed.
    ///
    /// @param paragraph the Paragraph instance representing the currently processed paragraph
    ///                                   in the document.
    public void setParagraph(Paragraph paragraph) {
        this.paragraph = paragraph;
    }
}
