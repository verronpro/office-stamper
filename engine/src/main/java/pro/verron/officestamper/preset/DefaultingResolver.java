package pro.verron.officestamper.preset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.Placeholder;

/**
 * The DefaultingResolver class provides a mechanism to handle exceptions by returning a default value.
 * It logs the exception message, and optionally, the stack trace if tracing is enabled.
 */
public class DefaultingResolver
        implements ExceptionResolver {

    private final String defaultValue;
    private final boolean tracing;
    private final Logger logger = LoggerFactory.getLogger(DefaultingResolver.class);

    public DefaultingResolver() {
        this("");
    }

    public DefaultingResolver(String defaultValue) {
        this(defaultValue, false);
    }

    public DefaultingResolver(String defaultValue, boolean tracing) {
        this.defaultValue = defaultValue;
        this.tracing = tracing;
    }

    public DefaultingResolver(boolean tracing) {
        this("", tracing);
    }

    @Override public String resolve(Placeholder placeholder, String message, Exception cause) {
        if (tracing) logger.warn(message, cause);
        else logger.warn(message);
        return defaultValue;
    }
}
