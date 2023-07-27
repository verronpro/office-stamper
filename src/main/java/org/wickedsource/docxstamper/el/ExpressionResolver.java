package org.wickedsource.docxstamper.el;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static org.wickedsource.docxstamper.el.ExpressionUtil.stripExpression;

/**
 * Resolves expressions against a given context object. Expressions can be either SpEL expressions or simple property
 * expressions.
 *
 * @author joseph
 * @version $Id: $Id
 */
public class ExpressionResolver {
	private final ExpressionParser parser;
	private final StandardEvaluationContext evaluationContext;

    /**
     * Creates a new ExpressionResolver with the given SpEL parser configuration.
     *
     * @param spelParserConfiguration   the configuration for the SpEL parser.
     * @param standardEvaluationContext a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object
     */
	public ExpressionResolver(
			StandardEvaluationContext standardEvaluationContext,
			SpelParserConfiguration spelParserConfiguration
	) {
		this.parser = new SpelExpressionParser(spelParserConfiguration);
		this.evaluationContext = standardEvaluationContext;
	}

	/**
	 * Runs the given expression against the given context object and returns the result of the evaluated expression.
	 *
	 * @param expressionString the expression to evaluate.
	 * @param contextRoot      the context object against which the expression is evaluated.
	 * @return the result of the evaluated expression.
	 */
	public Object resolveExpression(String expressionString, Object contextRoot) {
		if ((expressionString.startsWith("${") || expressionString.startsWith("#{")) && expressionString.endsWith("}")) {
			expressionString = stripExpression(expressionString);
		}
		evaluationContext.setRootObject(contextRoot);
		return parser.parseExpression(expressionString).getValue(evaluationContext);
	}
}
