package pro.verron.officestamper.core;

import pro.verron.officestamper.api.AnchoredComment;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.CommentProcessorContext;
import pro.verron.officestamper.api.Tag;

/// A hook that processes comments in a document.
public class CommentHook
        implements DocxHook {
    private final OfficePart part;
    private final Tag tag;
    private final AnchoredComment anchoredComment;

    CommentHook(OfficePart part, Tag tag, AnchoredComment anchoredComment) {
        this.part = part;
        this.tag = tag;
        this.anchoredComment = anchoredComment;
    }

    @Override
    public boolean run(
            EngineFactory engineFactory,
            ContextRoot contextRoot,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    ) {
        if (DocxHook.executed(tag)) return false;
        var paragraph = tag.paragraph();
        var expression = anchoredComment.expression();
        var contextKey = DocxHook.contextKey(tag);
        var contextStack = contextRoot.find(contextKey);
        var processorContext = new CommentProcessorContext(part, paragraph, anchoredComment, expression, contextStack);
        var evaluationContext = evaluationContextFactory.create(processorContext, contextStack);
        var engine = engineFactory.create(processorContext);
        var processed = engine.process(evaluationContext);
        tag.setAttribute("status", "executed");
        return processed;
    }

    @Override
    public void setContextKey(String contextKey) {
        tag.setAttribute("context", contextKey);
    }
}
