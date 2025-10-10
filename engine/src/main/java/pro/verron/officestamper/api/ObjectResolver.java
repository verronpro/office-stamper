package pro.verron.officestamper.api;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

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
     * A logger instance used for logging messages and events related to the operations
     * performed within the ObjectResolver interface.
     * This helps in tracking, debugging, and analyzing the execution of methods
     * implemented by classes that use this interface.
     */
    Logger LOGGER = LoggerFactory.getLogger(ObjectResolver.class);

    /**
     * Resolves the expression in the given document with the provided object.
     *
     * @param document    the {@link WordprocessingMLPackage} document in
     *                    which to resolve the expression
     * @param placeholder the expression value to be replaced
     * @param object      the object to be used for resolving the expression
     *
     * @return the resolved value for the expression
     *
     * @throws OfficeStamperException if no resolver is found for the object
     */
    default R resolve(DocxPart document, Placeholder placeholder, Object object) {
        R resolution = resolve(document, placeholder.content(), object);
        if (LOGGER.isDebugEnabled()) {
            var message = "Expression '{}' replaced by '{}' with resolver {}";
            var expression = placeholder.expression();
            var text = TextUtils.getText(resolution);
            var resolverName = getClass().getSimpleName();
            LOGGER.debug(message, expression, text, resolverName);
        }
        return resolution;
    }

    /**
     * Resolves the expression in the given document with the provided object.
     * <p>
     * Replace the previous {@link #resolve(WordprocessingMLPackage, String, Object)}
     *
     * @param docxPart   the {@link DocxPart} document in
     *                   which to resolve the expression
     * @param expression the expression value to be replaced
     * @param object     the object to be used for resolving the expression
     *
     * @return the resolved value for the expression
     *
     * @throws OfficeStamperException if no resolver is found for the object
     */
    default R resolve(DocxPart docxPart, String expression, Object object) {
        return resolve(docxPart.document(), expression, object);
    }

    /**
     * Resolves the expression in the given WordprocessingMLPackage document with the provided object.
     * @deprecated This method is deprecated and should not be called directly. It exists only for legacy
     * implementations that might still override it.
     *
     * @param document   the WordprocessingMLPackage document in which to resolve the expression
     * @param expression the expression value to be replaced
     * @param object     the object to be used for resolving the expression
     * @return the resolved value for the expression
     * @throws OfficeStamperException if this method is invoked directly
     */
    @Deprecated(since = "2.3", forRemoval = true)
    default R resolve(WordprocessingMLPackage document, String expression, Object object) {
        throw new OfficeStamperException("Should not be called, only legacy implementation might still override this");
    }

    /**
     * Checks if the given object can be resolved.
     *
     * @param object the object to be resolved
     *
     * @return true if the object can be resolved, false otherwise
     */
    boolean canResolve(@Nullable Object object);

}
