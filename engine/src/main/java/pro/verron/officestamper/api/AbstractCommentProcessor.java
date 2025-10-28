package pro.verron.officestamper.api;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.core.StandardParagraph;

import java.util.Objects;

/// AbstractCommentProcessor is an abstract base class for comment processors.
/// It implements the CommentProcessor interface.
/// It provides common functionality and fields that subclasses can use.
public abstract class AbstractCommentProcessor
        implements CommentProcessor {

    /// PlaceholderReplacer used to replace expressions in the comment text.
    protected final ParagraphPlaceholderReplacer placeholderReplacer;
    private Paragraph paragraph;
    /// @deprecated This method was only used by the "replaceWith" processor, which now can manage multiple runs at
    /// once, making this single-run tracking method obsolete
    @Deprecated(since = "2.10", forRemoval = true)
    private R currentRun;
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

    @Override public void setCurrentCommentWrapper(Comment currentComment) {
        Objects.requireNonNull(currentComment.getCommentRangeStart());
        Objects.requireNonNull(currentComment.getCommentRangeEnd());
        this.currentComment = currentComment;
    }

    @Override public void setProcessorContext(ProcessorContext processorContext) {
        setParagraph(processorContext.paragraph());
        setCurrentCommentWrapper(processorContext.comment());
    }

    /// Retrieves the current run being processed.
    ///
    /// @return the current [R] object being processed
    /// @deprecated This method was only used by the "replaceWith" processor, which now can manage multiple runs at
    /// once, making this single-run tracking method obsolete
    @Deprecated(since = "2.10", forRemoval = true)
    public R getCurrentRun() {
        return currentRun;
    }

    /// @deprecated This method was only used by the "replaceWith" processor, which now can manage multiple runs at
    /// once, making this single-run tracking method obsolete
    @Deprecated(since = "2.10", forRemoval = true)
    @Override public void setCurrentRun(@Nullable R run) {
        this.currentRun = run;
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

    /// @param paragraph coordinates of the currently processed paragraph within the template.
    ///
    /// @deprecated use [#setParagraph(Paragraph)] instead
    @Deprecated(since = "2.6", forRemoval = true) public void setParagraph(P paragraph) {
        this.paragraph = StandardParagraph.from((DocxPart) paragraph.getParent(), paragraph);
    }

    /// Sets the current paragraph being processed.
    ///
    /// @param paragraph the Paragraph instance representing the currently processed paragraph
    /// in the document.
    public void setParagraph(Paragraph paragraph) {
        this.paragraph = paragraph;
    }
}
