package pro.verron.officestamper.preset;

/**
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the
 * {@link pro.verron.officestamper.api.StringResolver} class instead.
 * This class will be removed in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public abstract class StringResolver<T>
        extends pro.verron.officestamper.api.StringResolver<T> {
    /**
     * Creates a new StringResolver with the given type.
     *
     * @param type the type of object to be resolved
     */
    protected StringResolver(Class<T> type) {
        super(type);
    }
}
