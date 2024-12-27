package pro.verron.officestamper.api;


/// ExceptionResolver is a functional interface used to resolve the behavior when an exception occurs during
/// the processing of a placeholder.
/// Implementations of this interface define how to handle the exception,
/// potentially logging the error, rethrowing the exception, or providing a fallback value.
@FunctionalInterface public interface ExceptionResolver {
    String resolve(Placeholder placeholder, String message, Exception cause);
}
