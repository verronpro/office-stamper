package pro.verron.officestamper.core;

import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.Placeholder;

import java.util.function.Function;

/// Resolves expressions against a given context object. Expressions can be either SpEL expressions or simple property
/// expressions.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class ExpressionParser {

    private final org.springframework.expression.ExpressionParser parser;
    private final Function<Object, EvaluationContext> evaluationContextSupplier;
    private EvaluationContext currentEvaluationContext;

    /// Creates a new ExpressionParser with the given SpEL parser configuration.
    ///
    /// @param evaluationContextSupplier a function providing an [EvaluationContext] when given a root object
    public ExpressionParser(
            Function<Object, EvaluationContext> evaluationContextSupplier,
            org.springframework.expression.ExpressionParser expressionParser
    ) {
        this.parser = expressionParser;
        this.evaluationContextSupplier = evaluationContextSupplier;
    }


    /// Resolves the content of a placeholder by evaluating the expression against the evaluation context.
    ///
    /// @param placeholder the placeholder to resolve
    ///
    /// @return the resolved value of the placeholder
    @Nullable
    public Object parse(Placeholder placeholder) {
        var expressionString = placeholder.content();
        var expression = parser.parseExpression(expressionString);
        return expression.getValue(currentEvaluationContext);
    }

    /// Sets the context object against which expressions will be resolved.
    ///
    /// @param contextRoot the context object to set as the root.
    public void setContext(Object contextRoot) {
        currentEvaluationContext = evaluationContextSupplier.apply(contextRoot);
    }
}
