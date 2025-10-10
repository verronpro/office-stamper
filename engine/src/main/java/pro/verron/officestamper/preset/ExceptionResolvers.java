package pro.verron.officestamper.preset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Placeholder;


/**
 * The ExceptionResolvers class provides a set of static factory methods to create different types of ExceptionResolver
 * implementations.
 * These resolvers are designed to handle exceptions that occur during the processing of placeholders in text
 * documents.
 * This class is a utility class and cannot be instantiated.
 */
public class ExceptionResolvers {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolvers.class);

    static {
        if (!logger.isTraceEnabled())
            logger.info("Set TRACE log level, to add stacktrace info to resolution exceptions");
    }

    private ExceptionResolvers() {
        throw new OfficeStamperException("Utility class");
    }

    /**
     * The passing resolver will handle exceptions by returning the placeholder expression.
     * It logs the exception message, and the stack trace if tracing is enabled.
     */
    public static ExceptionResolver passing() {
        return new PassingResolver(logger.isTraceEnabled());
    }

    /**
     * The defaulting resolver class will handle exceptions by returning an empty string.
     * It logs the exception message, and the stack trace if tracing is enabled.
     */
    public static ExceptionResolver defaulting() {
        return new DefaultingResolver("", logger.isTraceEnabled());
    }

    /**
     * The defaulting resolver class will handle exceptions by returning a default value.
     * It logs the exception message, and the stack trace if tracing is enabled.
     */
    public static ExceptionResolver defaulting(String value) {
        return new DefaultingResolver(value, logger.isTraceEnabled());
    }

    /**
     * The throwing resolver will handle exceptions by immediately throwing an OfficeStamperException.
     * It is used to propagate errors encountered during the processing of placeholders in text documents.
     */
    public static ExceptionResolver throwing() {
        return new ThrowingResolver(logger.isTraceEnabled());
    }

        /// Provides a legacy behavior for creating an appropriate `ExceptionResolver` based on the input parameters.
        /// @deprecated This method is deprecated and scheduled for removal in future versions. Use more specific factory
        /// methods instead.
        ///
        /// @param shouldFail       if `true`, returns a resolver that throws an exception.
        /// @param emptyOnError     if `true`, returns a resolver that provides an empty string on error.
        /// @param shouldReplace    if `true`, returns a resolver that uses the specified replacement value.
        /// @param replacementValue the replacement value to be used if `shouldReplace` is `true`.
        ///                         This parameter is ignored if `shouldReplace` is `false`.
        /// @return an `ExceptionResolver` instance that behaves according to the specified parameters.
        @Deprecated(since="2.5", forRemoval = true)
    public static ExceptionResolver legacyBehavior(
            boolean shouldFail,
            boolean emptyOnError,
            boolean shouldReplace,
            String replacementValue
    ) {
        if (shouldFail) return new ThrowingResolver(logger.isTraceEnabled());
        if (emptyOnError) return new DefaultingResolver("", logger.isTraceEnabled());
        if (shouldReplace) return new DefaultingResolver(replacementValue, logger.isTraceEnabled());
        return new PassingResolver(logger.isTraceEnabled());
    }

    private record DefaultingResolver(String value, boolean tracing)
            implements ExceptionResolver {

        private static final Logger logger = LoggerFactory.getLogger(DefaultingResolver.class);

        @Override public String resolve(Placeholder placeholder, String message, Exception cause) {
            if (tracing) logger.warn(message, cause);
            else logger.warn(message);
            return value;
        }
    }

    private record PassingResolver(boolean tracing)
            implements ExceptionResolver {

        @Override public String resolve(Placeholder placeholder, String message, Exception cause) {
            if (tracing) logger.warn(message, cause);
            else logger.warn(message);
            return placeholder.expression();
        }
    }

    private record ThrowingResolver(boolean tracing)
            implements ExceptionResolver {

        @Override public String resolve(Placeholder placeholder, String message, Exception cause) {
            if (tracing) throw new OfficeStamperException(message, cause);
            else throw new OfficeStamperException(message);
        }
    }
}
