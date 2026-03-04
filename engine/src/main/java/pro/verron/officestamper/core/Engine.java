package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import pro.verron.officestamper.api.*;

/// The core engine of OfficeStamper, responsible for processing expressions.
public class Engine {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);

    private final SpelParserConfiguration parserConfiguration;
    private final ExceptionResolver exceptionResolver;
    private final ObjectResolverRegistry objectResolverRegistry;
    private final ProcessorContext processorContext;
    private final String expression;
    private final DocxPart docxPart;
    private final SpelExpressionParser expressionParser;

    /// Constructs an Engine.
    ///
    /// @param parserConfiguration the parser configuration.
    /// @param exceptionResolver the exception resolver.
    /// @param objectResolverRegistry the object resolver registry.
    /// @param processorContext the processor context.
    public Engine(
            SpelParserConfiguration parserConfiguration,
            ExceptionResolver exceptionResolver,
            ObjectResolverRegistry objectResolverRegistry,
            ProcessorContext processorContext
    ) {
        this.parserConfiguration = parserConfiguration;
        this.exceptionResolver = exceptionResolver;
        this.objectResolverRegistry = objectResolverRegistry;
        this.processorContext = processorContext;
        this.expression = processorContext.expression();
        this.docxPart = processorContext.part();
        this.expressionParser = new SpelExpressionParser(parserConfiguration);
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
    public boolean process(UnionEvaluationContext evaluationContext) {
        try {
            var parsedExpression = expressionParser.parseRaw(expression);
            parsedExpression.getValue(evaluationContext);
            log.debug("Processed '{}' successfully.", expression);
            return true;
        } catch (SpelEvaluationException | SpelParseException e) {
            var msgTemplate = "Expression %s could not be processed against context '%s'";
            var message = msgTemplate.formatted(expression, evaluationContext);
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
    public Insert resolve(UnionEvaluationContext evaluationContext) {
        try {
            var parsedExpression = expressionParser.parseRaw(expression);
            var parsedAst = parsedExpression.getAST();
            var expressionState = new ExpressionState(evaluationContext, parserConfiguration);
            expressionState.pushActiveContextObject(evaluationContext.getLeafObject());
            var resolution = parsedAst.getValue(expressionState);
            var resolve = objectResolverRegistry.resolve(docxPart, expression, resolution);
            log.debug("Resolved '{}' successfully.", expression);
            return resolve;
        } catch (SpelEvaluationException | SpelParseException | OfficeStamperException e) {
            var msgTemplate = "Expression %s could not be resolved against context '%s'";
            var message = msgTemplate.formatted(expression, evaluationContext);
            return exceptionResolver.resolve(expression, message, e);
        }
    }
}
