package pro.verron.officestamper.core;

import pro.verron.officestamper.api.AnchoredComment;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.ProcessorContext;
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
        if ("executed".equals(tag.attribute("status"))) return false;
        var paragraph = tag.paragraph();
        var expression = anchoredComment.expression();
        var contextKey = contextKey(tag);
        var contextStack = contextRoot.find(contextKey);
        var processorContext = new ProcessorContext(part, paragraph, anchoredComment, expression, contextStack);
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

    /// Normalizes the context attribute of the tag: absent or non-numeric values resolve to "0".
    private static String contextKey(Tag tag) {
        var value = tag.attribute("context");
        if (value == null) return String.valueOf(0);
        try {
            return String.valueOf(Integer.parseInt(value));
        } catch (NumberFormatException _) {
            return String.valueOf(0);
        }
    }
}
