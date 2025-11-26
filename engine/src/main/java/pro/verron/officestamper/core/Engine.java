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
            expressionResolver.resolve(contextRoot, commentPlaceholder);
            log.debug("Processed '{}' successfully.", commentPlaceholder);
            return true;
        } catch (SpelEvaluationException | SpelParseException e) {
            var message = "Processing '%s' failed.".formatted(commentPlaceholder.expression());
            exceptionResolver.resolve(commentPlaceholder, message, e);
            return false;
        }
    }

    /// Resolves and replaces placeholder expressions within a specified paragraph tag.
    /// This method uses the provided context and document part to find and substitute
    /// placeholder expressions in the given tag with the corresponding resolved values.
    ///
    /// @param part        the part of the document containing the paragraph and associated content.
    /// @param tag         the tag representing the placeholder to be resolved and replaced.
    /// @param contextRoot the context object that provides data for resolving the placeholder expressions.
    public Insert resolve(DocxPart part, Tag tag, Object contextRoot) {
        Placeholder placeholder = tag.asPlaceholder();
        try {
            var resolution = expressionResolver.resolve(contextRoot, placeholder);
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
