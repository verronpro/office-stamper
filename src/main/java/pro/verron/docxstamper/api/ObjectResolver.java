package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.api.DocxStamperException;

/**
 * The ObjectResolver interface provides a contract for resolving objects to create a run
 * with the resolved content. It includes methods to check if an object can be resolved
 * and to actually resolve an object to a run.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.7
 */
public interface ObjectResolver {

    /**
     * Resolves the expression in the given document with the provided object.
     *
     * @param document   the {@link WordprocessingMLPackage} document in
     *                   which to resolve the expression
     * @param placeholder the expression value to be replaced
     * @param object     the object to be used for resolving the expression
     * @return the resolved value for the expression
     * @throws DocxStamperException if no resolver is found for the object
     */
    default R resolve(
            WordprocessingMLPackage document,
            Placeholder placeholder,
            Object object
    ) {
        R resolution = resolve(document, placeholder.content(), object);
        var msg = "Expression '{}' replaced by '{}' with resolver {}";
        Log.log.debug(msg, placeholder, resolution, this);
        return resolution;
    }

    /**
     * Checks if the given object can be resolved.
     *
     * @param object the object to be resolved
     * @return true if the object can be resolved, false otherwise
     */
    boolean canResolve(Object object);

    class Log {
        static Logger log = LoggerFactory.getLogger(ObjectResolver.class);
    }

    /**
     * Resolves the expression in the given document with the provided object.
     *
     * @param document   the {@link WordprocessingMLPackage} document in
     *                   which to resolve the expression
     * @param expression the expression value to be replaced
     * @param object     the object to be used for resolving the expression
     * @return the resolved value for the expression
     * @throws DocxStamperException if no resolver is found for the object
     */
    R resolve(
            WordprocessingMLPackage document,
            String expression,
            Object object
    );
}
