package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ProcessorContext;

import java.util.Objects;

/// The core engine of OfficeStamper, responsible for processing expressions.
public class Engine {
    private static final Logger log = LoggerFactory.getLogger(Engine.class);

    private final SpelParserConfiguration parserConfiguration;
    private final ExceptionResolver exceptionResolver;
    private final ObjectResolverRegistry objectResolverRegistry;
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
    public boolean process(EvaluationContext evaluationContext) {
        SpelNode spelNode;
        try {
            spelNode = parseAST();
            log.trace("Parsed '{}' successfully.", expression);
        } catch (SpelParseException e) {
            var msgTemplate = "Expression %s could not be parsed successfully.";
            var message = msgTemplate.formatted(expression, evaluationContext);
            exceptionResolver.resolve(expression, message, e);
            return false;
        }

        var expressionState = buildExpressionState(evaluationContext);
        try {
            spelNode.getValue(expressionState);
            log.debug("Processed '{}' successfully.", expression);
        } catch (SpelEvaluationException e) {
            var msgTemplate = "Expression %s could not be processed against context '%s'";
            var message = msgTemplate.formatted(expression, evaluationContext);
            exceptionResolver.resolve(expression, message, e);
            return false;
        }

        return true;
    }

    private SpelNode parseAST() {
        var parsedExpression = expressionParser.parseRaw(expression);
        return parsedExpression.getAST();
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
    ///
    /// @return an [Insert] object representing the resolved result of the expression within the context.
    public Insert resolve(EvaluationContext evaluationContext) {
        SpelNode spelNode;
        try {
            spelNode = parseAST();
            log.trace("Parsed '{}' successfully.", expression);
        } catch (SpelParseException e) {
            var msgTemplate = "Expression %s could not be parsed successfully.";
            var message = msgTemplate.formatted(expression, evaluationContext);
            return exceptionResolver.resolve(expression, message, e);
        }

        var expressionState = buildExpressionState(evaluationContext);
        Object javaResolution;
        try {
            javaResolution = spelNode.getValue(expressionState);
            log.debug("Resolved '{}' successfully.", expression);
        } catch (SpelEvaluationException e) {
            var msgTemplate = "Expression %s could not be resolved against context '%s'";
            var message = msgTemplate.formatted(expression, evaluationContext);
            return exceptionResolver.resolve(expression, message, e);
        }

        try {
            var docxResolution = objectResolverRegistry.resolve(docxPart, expression, javaResolution);
            log.debug("Converted '{}' to docx ({}) successfully.", expression, docxResolution);
            return docxResolution;
        } catch (SpelEvaluationException e) {
            var msgTemplate = "Expression %s could not be converted to docx inserts.";
            var message = msgTemplate.formatted(expression, evaluationContext);
            return exceptionResolver.resolve(expression, message, e);
        }
    }
}
