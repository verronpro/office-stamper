package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;

/**
 * The ObjectResolver interface provides a contract for resolving objects to create a run
 * with the resolved content. It includes methods to check if an object can be resolved
 * and to actually resolve an object to a run.
 *
 * @author Joseph Verron
 * @version 1.6.7
 * @since 1.6.7
 */
public interface ObjectResolver {
    /**
     * Checks if the given object can be resolved.
     *
     * @param object the object to be resolved
     * @return true if the object can be resolved, false otherwise
     */
    boolean canResolve(Object object);

    /**
     * Resolves the placeholder in the given document with the provided object.
     *
     * @param document    the {@link WordprocessingMLPackage} document in which to resolve the placeholder
     * @param placeholder the placeholder value to be replaced
     * @param object      the object to be used for resolving the placeholder
     * @return the resolved value for the placeholder
     * @throws DocxStamperException if no resolver is found for the object
     */
    R resolve(
            WordprocessingMLPackage document,
            String placeholder,
            Object object
    );
}
