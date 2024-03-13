package org.wickedsource.docxstamper.el;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.docxstamper.core.Expression;

/**
 * Resolves expressions against a given context object. Expressions can be either SpEL expressions or simple property
 * expressions.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class ExpressionResolver {

    private final ExpressionParser parser;
    private final StandardEvaluationContext evaluationContext;

    /**
     * Creates a new ExpressionResolver with the given SpEL parser configuration.
     *
     * @param spelParserConfiguration   the configuration for the SpEL parser.
     * @param standardEvaluationContext a {@link StandardEvaluationContext} object
     */
    public ExpressionResolver(
            StandardEvaluationContext standardEvaluationContext,
            SpelParserConfiguration spelParserConfiguration
    ) {
        this.parser = new SpelExpressionParser(spelParserConfiguration);
        this.evaluationContext = standardEvaluationContext;
    }

    /**
     * Resolves the given expression against the provided context object.
     *
     * @param expression   the expression to resolve.
     * @param contextRoot  the context object against which to resolve the expression.
     * @return the resolved value of the expression.
     */
    public Object resolve(Expression expression, Object contextRoot) {
        evaluationContext.setRootObject(contextRoot);
        return parser.parseExpression(expression.inner())
                .getValue(evaluationContext);
    }
}
