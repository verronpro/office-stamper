package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.CommentProcessorContext;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.Tag;

/// Hook for processing tags.
public class TagHook
        implements DocxHook {
    private static final Logger log = LoggerFactory.getLogger(TagHook.class);
    private final OfficePart part;
    private final Tag tag;

    TagHook(OfficePart part, Tag tag) {
        this.part = part;
        this.tag = tag;
    }

    @Override
    public boolean run(
            EngineFactory engineFactory,
            ContextRoot contextRoot,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    ) {
        if (DocxHook.executed(tag)) return false;
        var paragraph = tag.paragraph();
        var expression = tag.expression();
        var contextKey = DocxHook.contextKey(tag);
        var contextStack = contextRoot.find(contextKey);
        log.trace("Tag '{}' contextKey={} branch={}", expression, contextKey, contextStack);
        // The tag's comment-shaped object lets comment processors also process inline hook tags
        // (Tag.asComment always provides one, synthesised from the tag's own content when needed).
        var processorContext = new CommentProcessorContext(part, paragraph, tag.asComment(), expression, contextStack);
        var evaluationContext = evaluationContextFactory.create(processorContext, contextStack);
        var engine = engineFactory.create(processorContext);
        var tagType = tag.type();
        boolean processed = false;
        if ("inlineProcessor".equals(tagType)) {
            if (engine.process(evaluationContext)) processed = true;
            tag.remove();
        }
        else if ("placeholder".equals(tagType)) {
            var insert = engine.resolve(evaluationContext);
            processed = true;
            tag.replace(insert);
        }
        tag.setAttribute("status", "executed");
        return processed;
    }

    @Override
    public void setContextKey(String contextKey) {
        tag.setAttribute("context", contextKey);
    }
}
