package org.wickedsource.docxstamper.el;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.docxstamper.api.Placeholder;

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
     * @param placeholder   the expression to resolve.
     * @param contextRoot  the context object against which to resolve the expression.
     * @return the resolved value of the expression.
     */
    public Object resolve(Placeholder placeholder, Object contextRoot) {
        evaluationContext.setRootObject(contextRoot);
        return parser.parseExpression(placeholder.content())
                .getValue(evaluationContext);
    }
}
