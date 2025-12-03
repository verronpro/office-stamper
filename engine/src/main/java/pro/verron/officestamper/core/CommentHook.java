package pro.verron.officestamper.core;

import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ProcessorContext;

public class CommentHook
        implements Hook {
    private final DocxPart part;
    private final Comment comment;

    public CommentHook(DocxPart part, Comment comment) {
        this.part = part;
        this.comment = comment;
    }

    @Override
    public boolean run(
            EngineFactory engineFactory,
            ContextTree contextTree,
            EvaluationContextFactory evaluationContextMaker
    ) {
        var paragraph = comment.getParagraph();
        var expression = comment.expression();
        var contextStack = contextTree.find(comment.getContextReference());
        var processorContext = new ProcessorContext(part, paragraph, comment, expression, contextStack);
        var engine = engineFactory.create(processorContext);
        if (engine.process(evaluationContextMaker.create(processorContext, contextStack))) {
            CommentUtil.deleteComment(comment);
            return true;
        }
        return false;
    }

}
