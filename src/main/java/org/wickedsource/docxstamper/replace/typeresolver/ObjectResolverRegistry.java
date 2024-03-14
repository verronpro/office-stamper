package org.wickedsource.docxstamper.replace.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.api.ObjectResolver;
import pro.verron.docxstamper.core.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * A registry for object resolvers. It allows registering and resolving object resolvers based on certain criteria.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.7
 */
public class ObjectResolverRegistry {
    private static final Logger log = LoggerFactory.getLogger(
            ObjectResolverRegistry.class);
    private final List<ObjectResolver> resolvers = new ArrayList<>();

    /**
     * A registry for object resolvers. It allows registering and resolving object resolvers based on certain criteria.
     *
     * @param resolvers the ordered list of object resolvers to be registered in
     *                  the registry
     */
    public ObjectResolverRegistry(List<ObjectResolver> resolvers) {
        this.resolvers.addAll(resolvers);
    }

    /**
     * Resolves the expression in the given document with the provided object.
     *
     * @param document   the WordprocessingMLPackage document in which to resolve the placeholder
     * @param expression the expression value to be replaced
     * @param object     the object to be used for resolving the expression
     * @return the resolved value for the expression
     * @throws DocxStamperException if no resolver is found for the object
     */
    public R resolve(
            WordprocessingMLPackage document,
            Expression expression,
            Object object
    ) {
        for (ObjectResolver resolver : resolvers)
            if (resolver.canResolve(object))
                return resolver.resolve(document, expression, object);
        throw new DocxStamperException("No resolver for %s".formatted(object));
    }
}
