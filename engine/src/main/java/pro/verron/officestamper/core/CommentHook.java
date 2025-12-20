package pro.verron.officestamper.core;

import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ProcessorContext;

public class CommentHook
        implements Hook {
    private final DocxPart part;
    private final Tag tag;
    private final Comment comment;

    CommentHook(DocxPart part, Tag tag, Comment comment) {
        this.part = part;
        this.tag = tag;
        this.comment = comment;
    }

    @Override
    public boolean run(
            EngineFactory engineFactory,
            ContextTree contextTree,
            OfficeStamperEvaluationContextFactory officeStamperEvaluationContextFactory
    ) {
        var paragraph = comment.getParagraph();
        var expression = comment.expression();
        var contextKey = tag.getContextKey();
        var contextStack = contextTree.find(contextKey);
        var processorContext = new ProcessorContext(part, paragraph, comment, expression, contextStack);
        var evaluationContext = officeStamperEvaluationContextFactory.create(processorContext, contextStack);
        var engine = engineFactory.create(processorContext);
        if (engine.process(evaluationContext)) {
            CommentUtil.deleteComment(comment);
            return true;
        }
        return false;
    }

    @Override
    public void setContextKey(String contextKey) {
        tag.setContextKey(contextKey);
    }

}
