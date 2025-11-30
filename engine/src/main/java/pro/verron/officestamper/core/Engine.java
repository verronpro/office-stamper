package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.Inserts;
import pro.verron.officestamper.utils.WmlFactory;

public class Engine
        implements PlaceholderReplacer {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);

    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;
    private final ObjectResolverRegistry objectResolverRegistry;

    public Engine(
            ExpressionResolver expressionResolver,
            ExceptionResolver exceptionResolver,
            ObjectResolverRegistry objectResolverRegistry
    ) {
        this.expressionResolver = expressionResolver;
        this.exceptionResolver = exceptionResolver;
        this.objectResolverRegistry = objectResolverRegistry;
    }

    public boolean process(Object contextRoot, String expression) {
        try {
            expressionResolver.resolve(contextRoot, expression);
            log.debug("Processed '{}' successfully.", expression);
            return true;
        } catch (SpelEvaluationException | SpelParseException e) {
            var message = "Processing '%s' failed.".formatted(expression);
            exceptionResolver.resolve(expression, message, e);
            return false;
        }
    }

    /// Resolves a given expression against the provided context root, and a specific document part, returning an
    /// [Insert] object representing the resolved content.
    ///
    /// If an error occurs during resolution, attempts to generate a fallback resolution using an exception resolver.
    ///
    /// @param part the document part where the expression should be resolved
    /// @param expression the string expression to be evaluated
    /// @param contextRoot the root context object used for evaluating the expression
    ///
    /// @return an [Insert] object representing the resolved content, if resolution fails, returns an [Insert] created
    ///         with the exception resolver.
    public Insert resolve(DocxPart part, String expression, Object contextRoot) {
        try {
            var resolution = expressionResolver.resolve(contextRoot, expression);
            return objectResolverRegistry.resolve(part, expression, resolution);
        } catch (SpelEvaluationException | SpelParseException | OfficeStamperException e) {
            var msgTemplate = "Expression %s could not be resolved against context of type %s";
            var contextClass = contextRoot.getClass();
            var contextClassName = contextClass.getSimpleName();
            var message = msgTemplate.formatted(expression, contextClassName);
            var resolution = exceptionResolver.resolve(expression, message, e);
            return Inserts.of(WmlFactory.newRun(resolution));
        }
    }


}
