package pro.verron.officestamper.preset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.Placeholder;

/**
 * The PassingResolver class is an implementation of the ExceptionResolver interface
 * designed to handle exceptions by logging them and then returning the placeholder expression.
 */
public class PassingResolver
        implements ExceptionResolver {
    private final Logger logger = LoggerFactory.getLogger(PassingResolver.class);

    private final boolean tracing;

    public PassingResolver(boolean tracing) {this.tracing = tracing;}

    @Override public String resolve(Placeholder placeholder, String message, Exception cause) {
        if (tracing) logger.warn(message, cause);
        else logger.warn(message);
        return placeholder.expression();
    }
}
