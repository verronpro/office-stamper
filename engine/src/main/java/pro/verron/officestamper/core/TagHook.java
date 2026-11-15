package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.spi.OfficePart;
import pro.verron.officestamper.spi.ProcessorContext;
import pro.verron.officestamper.spi.Tag;

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
        if ("executed".equals(tag.attribute("status"))) return false;
        var AnchoredComment = tag.asComment();
        var paragraph = tag.paragraph();
        var expression = tag.expression();
        var contextKey = contextKey(tag);
        var contextStack = contextRoot.find(contextKey);
        log.trace("Tag '{}' contextKey={} branch={}", expression, contextKey, contextStack);
        var processorContext = new ProcessorContext(part, paragraph, AnchoredComment, expression, contextStack);
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

