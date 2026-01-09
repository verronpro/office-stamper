package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;

/// The core engine of OfficeStamper, responsible for processing expressions.
public class Engine {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);

    private final ExpressionParser expressionParser;
    private final ExceptionResolver exceptionResolver;
    private final ObjectResolverRegistry objectResolverRegistry;
    private final ProcessorContext processorContext;

    /// Constructs an Engine.
    ///
    /// @param expressionParser the expression parser.
    /// @param exceptionResolver the exception resolver.
    /// @param objectResolverRegistry the object resolver registry.
    /// @param processorContext the processor context.
    public Engine(
            ExpressionParser expressionParser,
            ExceptionResolver exceptionResolver,
            ObjectResolverRegistry objectResolverRegistry,
            ProcessorContext processorContext
    ) {
        this.expressionParser = expressionParser;
        this.exceptionResolver = exceptionResolver;
        this.objectResolverRegistry = objectResolverRegistry;
        this.processorContext = processorContext;
    }

    /// Processes the provided evaluation context against the expression defined in the processor context.
    ///
    /// The method attempts to resolve an expression using the given evaluation context.
    ///
    /// If successful, the process completes and logs a debug message.
    ///
    /// Otherwise, on failure ([SpelEvaluationException] or [SpelParseException]), it handles the exception by invoking
    /// the exceptionResolver and logs an error.
    ///
    /// @param evaluationContext the evaluation context for processing the expression.
    ///
    /// @return true if the processing was successful, otherwise false
    public boolean process(EvaluationContext evaluationContext) {
        var expression = processorContext.expression();
        try {
            var parsedExpression = expressionParser.parseExpression(expression);
            parsedExpression.getValue(evaluationContext);
            log.debug("Processed '{}' successfully.", expression);
            return true;
        } catch (SpelEvaluationException | SpelParseException e) {
            var message = "Processing '%s' failed.".formatted(expression);
            exceptionResolver.resolve(expression, message, e);
            return false;
        }
    }

    /// Resolves an [Insert] object by processing the provided evaluation context using the current processor context.
    /// Combines the processor context's part and expression with various resolvers to achieve the resolution.
    ///
    /// @param evaluationContext the evaluation context for processing the expression.
    ///
    /// @return an [Insert] object representing the resolved result of the expression within the context.
    public Insert resolve(EvaluationContext evaluationContext) {
        var part = processorContext.part();
        var expression = processorContext.expression();
        try {

            var parsedExpression = expressionParser.parseExpression(expression);
            var resolution = parsedExpression.getValue(evaluationContext);
            return objectResolverRegistry.resolve(part, expression, resolution);
        } catch (SpelEvaluationException | SpelParseException | OfficeStamperException e) {
            var msgTemplate = "Expression %s could not be resolved against context '%s'";
            var message = msgTemplate.formatted(expression, evaluationContext);
            return exceptionResolver.resolve(expression, message, e);
        }
    }
}
