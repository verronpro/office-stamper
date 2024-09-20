package pro.verron.officestamper.preset;


import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Placeholder;

/**
 * ThrowingResolver is an implementation of the ExceptionResolver interface designed to handle exceptions
 * by immediately throwing an OfficeStamperException. This class is used to propagate errors encountered
 * during the processing of placeholders in text documents.
 */
public class ThrowingResolver
        implements ExceptionResolver {
    private final boolean tracing;

    public ThrowingResolver(boolean tracing) {this.tracing = tracing;}

    @Override public String resolve(Placeholder placeholder, String message, Exception cause) {
        if (tracing) throw new OfficeStamperException(message, cause);
        else throw new OfficeStamperException(message);
    }
}
