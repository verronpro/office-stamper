package pro.verron.officestamper.api;


/// ExceptionResolver is a functional interface used to resolve the behavior when an exception occurs during the
/// processing of a placeholder.
///
/// Implementations of this interface define how to handle the exception, logging the error, rethrowing the exception,
/// or providing a fallback value.
@FunctionalInterface public interface ExceptionResolver {

    /// Resolves the given expression by providing a result or handling an exception that occurred during placeholder
    /// processing within a document. It allows implementations to log errors, rethrow exceptions, or return fallback
    /// values.
    ///
    /// @param expression the placeholder expression that was being evaluated when the exception occurred.
    /// @param message a descriptive message providing context about the exception
    /// @param cause the underlying exception that was encountered
    ///
    /// @return a resolved fallback value as a String, which could be used as a replacement for the placeholder.
    Insert resolve(String expression, String message, Exception cause);
}
