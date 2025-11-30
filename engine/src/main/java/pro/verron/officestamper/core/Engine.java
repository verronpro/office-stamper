package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.Inserts;
import pro.verron.officestamper.utils.WmlFactory;

public class Engine {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);

    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;
    private final ObjectResolverRegistry objectResolverRegistry;
    private final ProcessorContext processorContext;

    public Engine(
            ExpressionResolver expressionResolver,
            ExceptionResolver exceptionResolver,
            ObjectResolverRegistry objectResolverRegistry,
            ProcessorContext processorContext
    ) {
        this.expressionResolver = expressionResolver;
        this.exceptionResolver = exceptionResolver;
        this.objectResolverRegistry = objectResolverRegistry;
        this.processorContext = processorContext;
    }

    /// Processes the provided context object against the expression defined in the processor context.
    ///
    /// The method attempts to resolve an expression for the given context.
    ///
    /// If successful, the process completes and logs an informational message.
    ///
    /// Otherwise, in the case of a failure, it handles the exception by invoking the exceptionResolver and logs an
    /// error.
    ///
    /// @param contextRoot the root object containing the evaluation context for processing the expression.
    ///
    /// @return true if the processing was successful, otherwise false
    public boolean process(Object contextRoot) {
        var expression = processorContext.expression();
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

    /// Resolves an [Insert] object by processing the provided context root using the current processor context.
    /// Combines the processor context's part and expression with various resolvers to achieve the resolution.
    ///
    /// @param contextRoot the root object containing the evaluation context for processing the expression.
    ///
    /// @return an [Insert] object representing the resolved result of the expression within the context.
    public Insert resolve(Object contextRoot) {
        var part = processorContext.part();
        var expression = processorContext.expression();
        return resolve(part, contextRoot, expression, expressionResolver, objectResolverRegistry, exceptionResolver);
    }

    // TODO move this to a better place,or remove placeholder replacer concept
    static Insert resolve(
            DocxPart part,
            Object contextRoot,
            String expression,
            ExpressionResolver expressionResolver,
            ObjectResolverRegistry objectResolverRegistry,
            ExceptionResolver exceptionResolver
    ) {
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
