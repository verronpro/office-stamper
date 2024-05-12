package org.wickedsource.docxstamper.replace.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.officestamper.api.ITypeResolver;
import pro.verron.officestamper.api.ObjectResolver;

/**
 * The TypeResolver class is responsible for resolving objects of a specified type to objects of the DOCX4J API
 * that can be placed in a .docx document. It implements the ObjectResolver interface.
 *
 * @param type      the class type this TypeResolver is responsible for resolving.
 * @param resolver  the resolver to resolve objects of the given type.
 * @param nullProof a boolean value indicating whether the resolver is null-proof.
 * @param <T>       the type of the object this TypeResolver is responsible for resolving.
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.7
 * @deprecated This class's been deprecated since version 1.6.7
 * and will be removed in a future release.
 * Use the {@link ObjectResolver} interface instead.
 */
@Deprecated(since = "1.6.7", forRemoval = true)
public record TypeResolver<T>(
        Class<T> type,
        ITypeResolver<? super T> resolver,
        boolean nullProof
)
        implements ObjectResolver {

    /**
     * Constructs a TypeResolver object with the specified type and resolver.
     *
     * @param type     the class type this TypeResolver is responsible for resolving.
     * @param resolver the resolver to resolve objects of the given type.
     */
    public TypeResolver(
            Class<T> type,
            ITypeResolver<? super T> resolver
    ) {
        this(type, resolver, false);
    }

    /**
     * Determines whether the given object can be resolved by the TypeResolver.
     *
     * @param object the object to be resolved
     * @return true if the object can be resolved, false otherwise
     */
    @Override
    public boolean canResolve(@Nullable Object object) {
        if (object == null && this.nullProof)
            return true;
        return type.isInstance(object);
    }

    /**
     * Resolves an object of a specified type to an object of the DOCX4J API that can be placed in a .docx document.
     *
     * @param document   the WordprocessingMLPackage object representing the .docx document
     * @param expression the expression to be replaced in the .docx document
     * @param object     the object to be resolved
     * @return an object of the DOCX4J API that replaces the expression in the
     * .docx document
     * @throws DocxStamperException if the object is not an instance of the specified type
     */
    @Override
    public R resolve(
            WordprocessingMLPackage document,
            String expression,
            Object object
    ) {
        if (type.isInstance(object))
            return resolver.resolve(document, type.cast(object));

        String message = "%s was not an instance of %s"
                .formatted(object, type);
        throw new DocxStamperException(message);
    }
}
