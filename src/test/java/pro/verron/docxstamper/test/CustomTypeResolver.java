package pro.verron.docxstamper.test;

import pro.verron.docxstamper.api.StringResolver;

/**
 * <p>CustomTypeResolver class.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.6
 */
public class CustomTypeResolver
        extends StringResolver<Contexts.CustomType> {
    /**
     * The CustomTypeResolver class is a class that provides resolution of
     * an arbitrary custom type.
     * It extends the StringResolver class and is used to resolve strings for objects of type Contexts.CustomType.
     */
    public CustomTypeResolver() {
        super(Contexts.CustomType.class);
    }

    @Override
    protected String resolve(Contexts.CustomType object) {
        return "foo";
    }
}
