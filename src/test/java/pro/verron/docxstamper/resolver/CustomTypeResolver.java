package pro.verron.docxstamper.resolver;

import pro.verron.docxstamper.preset.resolver.StringResolver;
import pro.verron.docxstamper.utils.context.Contexts;

/**
 * <p>CustomTypeResolver class.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
public class CustomTypeResolver
        extends StringResolver<Contexts.CustomType> {
    public CustomTypeResolver() {
        super(Contexts.CustomType.class);
    }

    @Override
    protected String resolveStringForObject(Contexts.CustomType object) {
        return "foo";
    }
}
