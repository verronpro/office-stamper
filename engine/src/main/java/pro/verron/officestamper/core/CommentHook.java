package pro.verron.officestamper.core;

import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.utils.wml.WmlUtils;

/// A hook that processes comments in a document.
public class CommentHook
        implements DocxHook {
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
            ContextRoot contextRoot,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    ) {
        var comment = this.comment;
        var paragraph = tag.getParagraph();
        var expression = comment.expression();
        var contextKey = tag.getContextKey();
        var contextStack = contextRoot.find(contextKey);
        var processorContext = new ProcessorContext(part, paragraph, comment, expression, contextStack);
        var evaluationContext = evaluationContextFactory.create(processorContext, contextStack);
        var engine = engineFactory.create(processorContext);
        if (engine.process(evaluationContext)) {
            CommentUtil.deleteComment(comment);
            return true;
        }
        return false;
    }

    @Override
    public void setContextKey(String contextKey) {
        WmlUtils.setTagAttribute(tag.tag(), "context", contextKey);
    }

}
