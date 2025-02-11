package pro.verron.officestamper.api;

import org.docx4j.wml.R;
import org.springframework.lang.Nullable;

import java.util.Objects;

/// AbstractProcessor is an abstract base class for comment processors.
/// It implements the Processor interface.
/// It provides common functionality and fields that subclasses can use.
public abstract class AbstractProcessor
        implements Processor {

    private Paragraph paragraph;
    private R currentRun;
    private Comment currentComment;

    public Comment getCurrentCommentWrapper() {
        return currentComment;
    }

    @Override
    public void setProcessorContext(ProcessorContext processorContext) {
        setParagraph(processorContext.paragraph());
        setCurrentRun(processorContext.run());
        setCurrentCommentWrapper(processorContext.comment());
    }

    @Override
    public void setCurrentRun(@Nullable R run) {
        this.currentRun = run;
    }

    public R getCurrentRun() {
        return currentRun;
    }

    @Override
    public void setCurrentCommentWrapper(Comment currentComment) {
        Objects.requireNonNull(currentComment.getCommentRangeStart());
        Objects.requireNonNull(currentComment.getCommentRangeEnd());
        this.currentComment = currentComment;
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

    public void setParagraph(Paragraph paragraph) {
        this.paragraph = paragraph;
    }
}
