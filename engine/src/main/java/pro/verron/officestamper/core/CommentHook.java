package pro.verron.officestamper.core;

import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ProcessorContext;

import java.util.function.Function;

public class CommentHook
        implements Hook {
    private final DocxPart part;
    private final Comment comment;

    public CommentHook(DocxPart part, Comment comment) {
        this.part = part;
        this.comment = comment;
    }

    @Override
    public boolean run(Function<ProcessorContext, Engine> engineFactory, Object contextRoot) {
        var paragraph = comment.getParagraph(part);
        var placeholder = comment.asPlaceholder();
        var expression = placeholder.content();
        var processorContext = new ProcessorContext(part, paragraph, comment, placeholder);
        var engine = engineFactory.apply(processorContext);
        if (engine.process(contextRoot, expression)) {
            CommentUtil.deleteComment(comment);
            return true;
        }
        return false;
    }
}
