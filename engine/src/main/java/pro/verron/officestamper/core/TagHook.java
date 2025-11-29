package pro.verron.officestamper.core;

import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.api.Tag;

import java.util.function.Function;

public class TagHook
        implements Hook {
    private final Tag tag;
    private final DocxPart part;

    public TagHook(DocxPart part, Tag tag) {
        this.tag = tag;
        this.part = part;
    }

    @Override
    public boolean run(Function<ProcessorContext, Engine> engineFactory, Object contextRoot) {
        var comment = tag.asComment();
        var paragraph = tag.getParagraph();
        var placeholder = tag.asPlaceholder();
        var processorContext = new ProcessorContext(part, paragraph, comment, placeholder);
        var engine = engineFactory.apply(processorContext);
        var tagType = tag.type()
                         .orElse(null);
        boolean processed = false;
        if ("processor".equals(tagType)) {
            if (engine.process(contextRoot, placeholder)) processed = true;
            tag.remove();
        }
        else if ("placeholder".equals(tagType)) {
            var insert = engine.resolve(part, tag, contextRoot);
            processed = true;
            tag.replace(insert);
        }
        return processed;
    }
}
