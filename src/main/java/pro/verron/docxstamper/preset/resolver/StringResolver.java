package pro.verron.docxstamper.preset.resolver;

/**
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the
 * {@link pro.verron.docxstamper.api.StringResolver} class instead.
 * This class will be removed in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public abstract class StringResolver<T>
        extends pro.verron.docxstamper.api.StringResolver<T> {
    /**
     * Creates a new StringResolver with the given type.
     *
     * @param type the type of object to be resolved
     */
    protected StringResolver(Class<T> type) {
        super(type);
    }
}
