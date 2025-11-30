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

    public boolean process(Object contextRoot, Placeholder commentPlaceholder) {
        try {
            expressionResolver.resolve(contextRoot, commentPlaceholder.content());
            log.debug("Processed '{}' successfully.", commentPlaceholder);
            return true;
        } catch (SpelEvaluationException | SpelParseException e) {
            var message = "Processing '%s' failed.".formatted(commentPlaceholder.expression());
            exceptionResolver.resolve(commentPlaceholder, message, e);
            return false;
        }
    }

    /// Resolves a placeholder expression and returns an appropriate Insert object based on the resolution result. This
    /// method attempts to resolve the placeholder's content using the expression resolver against the provided context.
    /// If successful, the resolved object is further processed by the object resolver registry. If any error occurs
    /// during resolution, an error message is generated and processed through the exception resolver.
    ///
    /// @param part the document part containing the placeholder to be resolved
    /// @param placeholder the placeholder containing the expression to be resolved
    /// @param contextRoot the root object used as the evaluation context for the expression
    ///
    /// @return an Insert object containing either the successfully resolved content or an error message
    public Insert resolve(DocxPart part, Placeholder placeholder, Object contextRoot) {
        try {
            var resolution = expressionResolver.resolve(contextRoot, placeholder.content());
            return objectResolverRegistry.resolve(part, placeholder, resolution);
        } catch (SpelEvaluationException | SpelParseException | OfficeStamperException e) {
            var msgTemplate = "Expression %s could not be resolved against context of type %s";
            var contextClass = contextRoot.getClass();
            var contextClassName = contextClass.getSimpleName();
            var message = msgTemplate.formatted(placeholder.expression(), contextClassName);
            var resolution = exceptionResolver.resolve(placeholder, message, e);
            return Inserts.of(WmlFactory.newRun(resolution));
        }
    }


}
