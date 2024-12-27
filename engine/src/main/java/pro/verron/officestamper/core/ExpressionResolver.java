package pro.verron.officestamper.core;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.Placeholder;

/// Resolves expressions against a given context object. Expressions can be either SpEL expressions or simple property
/// expressions.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class ExpressionResolver {

    private final ExpressionParser parser;
    private final StandardEvaluationContext evaluationContext;

    /// Creates a new ExpressionResolver with the given SpEL parser configuration.
    ///
    /// @param standardEvaluationContext a [StandardEvaluationContext] object
    public ExpressionResolver(
            StandardEvaluationContext standardEvaluationContext,
            ExpressionParser expressionParser
    ) {
        this.parser = expressionParser;
        this.evaluationContext = standardEvaluationContext;
    }


    /// Resolves the content of a placeholder by evaluating the expression against the evaluation context.
    ///
    /// @param placeholder the placeholder to resolve
    ///
    /// @return the resolved value of the placeholder
    @Nullable public Object resolve(Placeholder placeholder) {
        var expressionString = placeholder.content();
        var expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext);
    }

    /// Sets the context object against which expressions will be resolved.
    ///
    /// @param contextRoot the context object to set as the root.
    public void setContext(Object contextRoot) {
        evaluationContext.setRootObject(contextRoot);
    }
}
