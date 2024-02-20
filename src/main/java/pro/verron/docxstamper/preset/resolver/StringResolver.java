package pro.verron.docxstamper.preset.resolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.docxstamper.api.ObjectResolver;

/**
 * A StringResolver is an abstract class that provides a generic implementation for resolving objects
 * to strings. It is used in conjunction with ObjectResolver interface to provide a flexible way to
 * resolve different types of objects to strings.
 *
 * @param <T> the type of the object to resolve
 */
public abstract class StringResolver<T>
        implements ObjectResolver {

    private final Class<T> type;

    protected StringResolver(Class<T> type) {
        assert type != null;
        this.type = type;
    }

    /**
     * Determines if the given object can be resolved by the StringResolver.
     *
     * @param object the object to be resolved
     * @return true if the object can be resolved, false otherwise
     */
    @Override
    public final boolean canResolve(Object object) {
        return type.isInstance(object);
    }

    /**
     * Resolves an object to a string and creates a new run with the resolved string as content.
     *
     * @param document    the WordprocessingMLPackage document
     * @param placeholder the placeholder string
     * @param object      the object to be resolved
     * @return the newly created run with the resolved string as content
     */
    @Override
    public R resolve(
            WordprocessingMLPackage document,
            String placeholder,
            Object object
    ) {
        return RunUtil.create(resolveStringForObject(type.cast(object)));
    }

    /**
     * Resolves an object to a string.
     *
     * @param object the object to be resolved
     * @return the string representation of the object
     */
    protected abstract String resolveStringForObject(T object);
}
