package pro.verron.officestamper.core;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.lang.Nullable;

/// Resolves expressions against a given context object. Expressions can be either SpEL expressions or simple property
/// expressions.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class ExpressionResolver {

    private final ExpressionParser parser;

    /// Creates a new ExpressionResolver with the given SpEL parser configuration.
    public ExpressionResolver(ExpressionParser expressionParser) {
        this.parser = expressionParser;
    }

    /// Resolves the specified expression against the given evaluation context.
    ///
    /// The method uses an expression parser to evaluate the provided expression string in the context of the
    /// evaluationContext object.
    ///
    /// The result of the evaluation is returned.
    ///
    /// @param evaluationContext the evaluation context used for the expression.
    /// @param expression the expression to be evaluated as a [String]
    ///
    /// @return the result of the evaluated expression, possibly. It will return `null` when the expression does not
    ///         have a return value, or when the result is the `null` value.
    @Nullable
    public Object resolve(EvaluationContext evaluationContext, String expression) {

        var parsedExpression = parser.parseExpression(expression);
        return parsedExpression.getValue(evaluationContext);
    }
}
