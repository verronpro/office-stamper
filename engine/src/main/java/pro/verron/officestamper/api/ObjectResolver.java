package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.lang.Nullable;

/// The ObjectResolver interface provides a contract for resolving objects to create a run
/// with the resolved content. It includes methods to check if an object can be resolved
/// and to actually resolve an object to a run.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public interface ObjectResolver {

    /// Resolves the expression in the given document with the provided object.
    ///
    /// Replace the previous [#resolve(WordprocessingMLPackage, String, Object)]
    ///
    /// @param docxPart   the [DocxPart] document in
    ///                   which to resolve the expression
    /// @param expression the expression value to be replaced
    /// @param object     the object to be used for resolving the expression
    ///
    /// @return the resolved value for the expression
    ///
    /// @throws OfficeStamperException if no resolver is found for the object
    Insert resolve(DocxPart docxPart, String expression, Object object);

    /// Checks if the given object can be resolved.
    ///
    /// @param object the object to be resolved
    ///
    /// @return true if the object can be resolved, false otherwise
    boolean canResolve(@Nullable Object object);
}
