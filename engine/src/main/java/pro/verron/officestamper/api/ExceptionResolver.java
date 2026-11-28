package pro.verron.officestamper.api;

import java.util.Optional;

/// ExceptionResolver is a functional interface used to resolve the behavior when an exception occurs during the
/// processing of a placeholder.
///
/// Implementations of this interface define how to handle the exception, logging the error, rethrowing the exception,
/// or providing a fallback value.
///
/// The resolver is deliberately decoupled from the document: it returns a plain fallback
/// string, and the engine builds the replacement element with its own part's element factory.
@FunctionalInterface public interface ExceptionResolver {

    /// Resolves the behavior when an exception occurred during placeholder processing within a document.
    /// It allows implementations to log errors, rethrow exceptions, or provide a fallback value.
    ///
    /// @param expression the placeholder expression that was being evaluated when the exception occurred.
    /// @param message a descriptive message providing context about the exception
    /// @param cause the underlying exception that was encountered
    /// @return an [Optional] carrying the fallback string to use as replacement content for the
    ///         placeholder, or empty when the resolver provides no fallback — in which case the
    ///         engine throws an [OfficeStamperException] carrying the same message.
    Optional<String> resolve(String expression, String message, Exception cause);
}
