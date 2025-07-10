package pro.verron.officestamper.api;


/**
 * ExceptionResolver is a functional interface used to resolve the behavior when an exception occurs during
 * the processing of a placeholder.
 * Implementations of this interface define how to handle the exception,
 * logging the error, rethrowing the exception, or providing a fallback value.
 */
@FunctionalInterface
public interface ExceptionResolver {
    /**
     * Resolves the behavior in handling exceptions during the processing of a placeholder.
     * This method is used to determine the appropriate behavior when an exception occurs,
     * such as returning a fallback value, logging the error, or rethrowing the exception.
     *
     * @param placeholder the placeholder being processed when the exception occurred
     * @param message     the message providing context or additional information about the exception
     * @param cause       the exception that occured during the placeholder processing
     *
     * @return a string representing the resolved outcome, such as an alternate response or error information.
     */
    String resolve(Placeholder placeholder, String message, Exception cause);
}
