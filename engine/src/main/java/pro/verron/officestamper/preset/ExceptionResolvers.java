package pro.verron.officestamper.preset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Placeholder;


/// The ExceptionResolvers class provides a set of static factory methods to create different types of ExceptionResolver
/// implementations.
/// These resolvers are designed to handle exceptions that occur during the processing of placeholders in text
/// documents.
/// This class is a utility class and cannot be instantiated.
public class ExceptionResolvers {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolvers.class);

    static {
        if (!logger.isTraceEnabled())
            logger.info("Set TRACE log level, to add stacktrace info to resolution exceptions");
    }

    private ExceptionResolvers() {
        throw new OfficeStamperException("Utility class");
    }

    /// The passing resolver will handle exceptions by returning the placeholder expression.
    /// It logs the exception message, and the stack trace if tracing is enabled.
    public static ExceptionResolver passing() {
        return new PassingResolver(logger.isTraceEnabled());
    }

    /// The defaulting resolver class will handle exceptions by returning an empty string.
    /// It logs the exception message, and the stack trace if tracing is enabled.
    public static ExceptionResolver defaulting() {
        return new DefaultingResolver("", logger.isTraceEnabled());
    }

    /// The defaulting resolver class will handle exceptions by returning a default value.
    /// It logs the exception message, and the stack trace if tracing is enabled.
    public static ExceptionResolver defaulting(String value) {
        return new DefaultingResolver(value, logger.isTraceEnabled());
    }

    /// The throwing resolver will handle exceptions by immediately throwing an OfficeStamperException.
    /// It is used to propagate errors encountered during the processing of placeholders in text documents.
    public static ExceptionResolver throwing() {
        return new ThrowingResolver(logger.isTraceEnabled());
    }

    private record DefaultingResolver(String value, boolean tracing)
            implements ExceptionResolver {

        private static final Logger logger = LoggerFactory.getLogger(DefaultingResolver.class);

        @Override
        public String resolve(Placeholder placeholder, String message, Exception cause) {
            if (tracing) logger.warn(message, cause);
            else logger.warn(message);
            return value;
        }
    }

    private record PassingResolver(boolean tracing)
            implements ExceptionResolver {

        @Override
        public String resolve(Placeholder placeholder, String message, Exception cause) {
            if (tracing) logger.warn(message, cause);
            else logger.warn(message);
            return placeholder.expression();
        }
    }

    private record ThrowingResolver(boolean tracing)
            implements ExceptionResolver {

        @Override
        public String resolve(Placeholder placeholder, String message, Exception cause) {
            if (tracing) throw new OfficeStamperException(message, cause);
            else throw new OfficeStamperException(message);
        }
    }
}
