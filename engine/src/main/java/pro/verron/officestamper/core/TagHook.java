package pro.verron.officestamper.core;

import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.utils.wml.WmlUtils;

public class TagHook
        implements DocxHook {
    private final Tag tag;
    private final DocxPart part;

    TagHook(DocxPart part, Tag tag) {
        this.tag = tag;
        this.part = part;
    }

    @Override
    public boolean run(
            EngineFactory engineFactory,
            ContextRoot contextRoot,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    ) {
        var comment = tag.asComment();
        var paragraph = tag.getParagraph();
        var expression = tag.expression();
        var contextKey = tag.getContextKey();
        var contextStack = contextRoot.find(contextKey);
        var processorContext = new ProcessorContext(part, paragraph, comment, expression, contextStack);
        var evaluationContext = evaluationContextFactory.create(processorContext, contextStack);
        var engine = engineFactory.create(processorContext);
        var tagType = tag.type()
                         .orElse(null);
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
        return processed;
    }

    @Override
    public void setContextKey(String contextKey) {
        WmlUtils.setTagAttribute(tag.tag(), "context", contextKey);
    }

}
