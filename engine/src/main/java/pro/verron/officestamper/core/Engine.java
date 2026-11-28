package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ProcessorContext;

import java.util.Objects;
import java.util.stream.Collectors;

/// The core engine of OfficeStamper, responsible for processing expressions.
public class Engine {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);

    private final SpelParserConfiguration parserConfiguration;
    private final ExceptionResolver exceptionResolver;
    private final ObjectResolverRegistry objectResolverRegistry;
    private final String expression;
    private final OfficePart part;
    private final SpelExpressionParser expressionParser;
    private final TraceabilityReporter traceabilityReporter;

    /// Constructs an Engine.
    ///
    /// @param parserConfiguration the parser configuration.
    /// @param exceptionResolver the exception resolver.
    /// @param objectResolverRegistry the object resolver registry.
    /// @param processorContext the processor context.
    /// @param traceabilityReporter the traceability reporter.
    public Engine(
            SpelParserConfiguration parserConfiguration,
            ExceptionResolver exceptionResolver,
            ObjectResolverRegistry objectResolverRegistry,
            ProcessorContext processorContext,
            TraceabilityReporter traceabilityReporter
    ) {
        this.parserConfiguration = parserConfiguration;
        this.expressionParser = new SpelExpressionParser(parserConfiguration);
        this.exceptionResolver = exceptionResolver;
        this.objectResolverRegistry = objectResolverRegistry;
        this.expression = processorContext.expression();
        this.part = processorContext.part();
        this.traceabilityReporter = traceabilityReporter;
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
    /// @return true if the processing was successful, otherwise false
    public boolean process(EvaluationContext evaluationContext) {
        SpelNode spelNode;
        try {
            spelNode = parseAST(expressionParser, expression);
        } catch (SpelParseException e) {
            var message = "Expression %s could not be parsed successfully.".formatted(expression);
            fallback(message, e);
            return false;
        }

        var expressionState = buildExpressionState(evaluationContext);
        try {
            var value = spelNode.getValue(expressionState);
            log.debug("Processed '{}' successfully.", expression);
            var contextBranch = (ContextBranch) Objects.requireNonNull(evaluationContext.getRootObject().getValue());
            traceabilityReporter.onResolution(expression, value, contextBranch.stream().collect(Collectors.toList()));
        } catch (SpelEvaluationException e) {
            var message = "Expression %s could not be processed against context %s"
                    .formatted(expression, contextDescription(evaluationContext));
            fallback(message, e);
            return false;
        }

        return true;
    }

    private static SpelNode parseAST(SpelExpressionParser parser, String expression) {
        var parsedExpression = parser.parseRaw(expression);
        log.trace("Parsed '{}' successfully.", expression);
        return parsedExpression.getAST();
    }

    /// Describes the context branch of an evaluation context for error messages, without
    /// embedding the whole evaluation context's toString.
    private static String contextDescription(EvaluationContext evaluationContext) {
        var root = evaluationContext.getRootObject()
                                    .getValue();
        return root instanceof ContextBranch branch ? branch.toString() : String.valueOf(root);
    }

    /// Hands the failure to the configured [ExceptionResolver] and builds the fallback
    /// [Insert] from its answer. A resolver that provides no fallback (empty optional)
    /// surfaces as an [OfficeStamperException] carrying the same message.
    private Insert fallback(String message, Exception cause) {
        var fallback = exceptionResolver.resolve(expression, message, cause)
                                        .orElseThrow(() -> new OfficeStamperException(message, cause));
        return new Insert(part.factory().run(fallback));
    }

    private ExpressionState buildExpressionState(EvaluationContext evaluationContext) {
        var contextBranchTypedValue = evaluationContext.getRootObject();
        var contextBranch = (ContextBranch) Objects.requireNonNull(contextBranchTypedValue.getValue());
        var rootObject = contextBranch.root();
        var rootObjectTypedValue = new TypedValue(rootObject);
        var expressionState = new ExpressionState(evaluationContext, rootObjectTypedValue, parserConfiguration);
        for (Object o : contextBranch) {
            expressionState.pushActiveContextObject(new TypedValue(o));
            expressionState.enterScope();
        }
        return expressionState;
    }

    /// Resolves an [Insert] object by processing the provided evaluation context using the current processor context.
    /// Combines the processor context's part and expression with various resolvers to achieve the resolution.
    ///
    /// @param evaluationContext the evaluation context for processing the expression.
    /// @return an [Insert] object representing the resolved result of the expression within the context.
    public Insert resolve(EvaluationContext evaluationContext) {
        SpelNode spelNode;
        try {
            spelNode = parseAST(expressionParser, expression);
        } catch (SpelParseException e) {
            var message = "Expression %s could not be parsed successfully.".formatted(expression);
            return fallback(message, e);
        }

        var expressionState = buildExpressionState(evaluationContext);
        Object javaResolution;
        try {
            javaResolution = spelNode.getValue(expressionState);
            log.debug("Resolved '{}' successfully.", expression);
            var contextBranch = (ContextBranch) Objects.requireNonNull(evaluationContext.getRootObject().getValue());
            traceabilityReporter.onResolution(expression, javaResolution, contextBranch.stream().collect(Collectors.toList()));
        } catch (SpelEvaluationException e) {
            var message = "Expression %s could not be resolved against context %s"
                    .formatted(expression, contextDescription(evaluationContext));
            return fallback(message, e);
        }

        try {
            var docxResolution = objectResolverRegistry.resolve(part, expression, javaResolution);
            log.debug("Converted '{}' to docx ({}) successfully.", expression, docxResolution);
            return docxResolution;
        } catch (SpelEvaluationException e) {
            var message = "Expression %s could not be converted to docx inserts.".formatted(expression);
            return fallback(message, e);
        }
    }
}
