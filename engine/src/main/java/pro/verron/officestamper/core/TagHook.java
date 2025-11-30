package pro.verron.officestamper.core;

import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.api.Tag;

public class TagHook
        implements Hook {
    private final Tag tag;
    private final DocxPart part;

    public TagHook(DocxPart part, Tag tag) {
        this.tag = tag;
        this.part = part;
    }

    @Override
    public boolean run(EngineFactory engineFactory, Object contextRoot) {
        var comment = tag.asComment();
        var paragraph = tag.getParagraph();
        var placeholder = tag.asPlaceholder();
        var expression = placeholder.content();
        var processorContext = new ProcessorContext(part, paragraph, comment, placeholder);
        var engine = engineFactory.create(processorContext);
        var tagType = tag.type()
                         .orElse(null);
        boolean processed = false;
        if ("processor".equals(tagType)) {
            if (engine.process(contextRoot, expression)) processed = true;
            tag.remove();
        }
        else if ("placeholder".equals(tagType)) {
            var insert = engine.resolve(part, expression, contextRoot);
            processed = true;
            tag.replace(insert);
        }
        return processed;
    }
}
