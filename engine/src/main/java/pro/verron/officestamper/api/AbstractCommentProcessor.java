package pro.verron.officestamper.api;

import org.docx4j.wml.R;
import org.springframework.lang.Nullable;

import java.util.Objects;

/// AbstractCommentProcessor is an abstract base class for comment processors.
/// It implements the CommentProcessor interface.
/// It provides common functionality and fields that subclasses can use.
public abstract class AbstractCommentProcessor
        implements CommentProcessor {

    /// PlaceholderReplacer used to replace expressions in the comment text.
    protected final ParagraphPlaceholderReplacer placeholderReplacer;
    private Paragraph paragraph;
    private R currentRun;
    private Comment currentComment;

    /// Creates an instance of AbstractCommentProcessor with the given ParagraphPlaceholderReplacer.
    ///
    /// @param placeholderReplacer the ParagraphPlaceholderReplacer used to replace expressions in the comment text
    protected AbstractCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        this.placeholderReplacer = placeholderReplacer;
    }

    public Comment getCurrentCommentWrapper() {
        return currentComment;
    }

    @Override public void setCurrentCommentWrapper(Comment currentComment) {
        Objects.requireNonNull(currentComment.getCommentRangeStart());
        Objects.requireNonNull(currentComment.getCommentRangeEnd());
        this.currentComment = currentComment;
    }

    @Override public void setProcessorContext(ProcessorContext processorContext) {
        setParagraph(processorContext.paragraph());
        setCurrentRun(processorContext.run());
        setCurrentCommentWrapper(processorContext.comment());
    }

    public R getCurrentRun() {
        return currentRun;
    }

    @Override public void setCurrentRun(@Nullable R run) {
        this.currentRun = run;
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

    public void setParagraph(Paragraph paragraph) {
        this.paragraph = paragraph;
    }
}
