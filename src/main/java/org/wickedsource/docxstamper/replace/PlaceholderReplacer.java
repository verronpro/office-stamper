package org.wickedsource.docxstamper.replace;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.el.ExpressionResolver;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import org.wickedsource.docxstamper.util.RunUtil;
import org.wickedsource.docxstamper.util.walk.BaseCoordinatesWalker;
import pro.verron.docxstamper.core.Expression;
import pro.verron.docxstamper.core.Expressions;
import pro.verron.docxstamper.core.ObjectResolverRegistry;

/**
 * Replaces expressions in a document with the values provided by the {@link ExpressionResolver}.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class PlaceholderReplacer {
    private static final Logger log = LoggerFactory.getLogger(
            PlaceholderReplacer.class);
    private final ExpressionResolver resolver;
    private final ObjectResolverRegistry registry;
    private final boolean failOnUnresolvedExpression;
    private final boolean leaveEmptyOnExpressionError;
    private final boolean replaceUnresolvedExpressions;
    private final String unresolvedExpressionsDefaultValue;
    private final Expression lineBreakPlaceholder;

    /**
     * <p>Constructor for PlaceholderReplacer.</p>
     *
     * @param registry                          the registry containing all available type resolvers.
     * @param resolver                          the expression resolver used to resolve expressions in the document.
     * @param failOnUnresolvedExpression        if set to true, an exception is thrown when an expression cannot be
     *                                          resolved.
     * @param replaceUnresolvedExpressions      if set to true, expressions that cannot be resolved are replaced by the
     *                                          value provided in the unresolvedExpressionsDefaultValue parameter.
     * @param unresolvedExpressionsDefaultValue the value to use when replacing unresolved expressions.
     * @param leaveEmptyOnExpressionError       if set to true, expressions
     *                                          that cannot be resolved will
     *                                          be by replaced by an
     *                                          empty string.
     * @param lineBreakExpression               if set to a non-null value,
     *                                          all occurrences of this placeholder will be
     *                                          replaced with a line break.
     */
    public PlaceholderReplacer(
            ObjectResolverRegistry registry,
            ExpressionResolver resolver,
            boolean failOnUnresolvedExpression,
            boolean replaceUnresolvedExpressions,
            String unresolvedExpressionsDefaultValue,
            boolean leaveEmptyOnExpressionError,
            Expression lineBreakExpression
    ) {
        this.registry = registry;
        this.resolver = resolver;
        this.failOnUnresolvedExpression = failOnUnresolvedExpression;
        this.replaceUnresolvedExpressions = replaceUnresolvedExpressions;
        this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue;
        this.leaveEmptyOnExpressionError = leaveEmptyOnExpressionError;
        this.lineBreakPlaceholder = lineBreakExpression;
    }

    /**
     * Finds expressions in a document and resolves them against the specified context object.
     * The resolved values will then replace the expressions in the document.
     *
     * @param document          the document in which to replace all expressions.
     * @param expressionContext the context root
     */
    public void resolveExpressions(
            final WordprocessingMLPackage document,
            Object expressionContext
    ) {
        new BaseCoordinatesWalker() {
            @Override
            protected void onParagraph(P paragraph) {
                resolveExpressionsForParagraph(
                        new ParagraphWrapper(paragraph),
                        expressionContext,
                        document);
            }
        }.walk(document);
    }

    /**
     * Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
     *
     * @param paragraph the paragraph in which to replace expressions.
     * @param context   the context root
     * @param document  the document in which to replace all expressions.
     */
    public void resolveExpressionsForParagraph(
            ParagraphWrapper paragraph,
            Object context,
            WordprocessingMLPackage document
    ) {
        var expressions = Expressions.findVariables(paragraph);
        for (var expression : expressions) {
            try {
                var resolution = resolver.resolve(expression, context);
                var replacement = registry.resolve(document, expression,
                                                   resolution);
                paragraph.replace(expression, replacement);
            } catch (SpelEvaluationException | SpelParseException e) {
                if (failOnUnresolvedExpression) {
                    String message = "Expression %s could not be resolved against context of type %s"
                            .formatted(expression, context.getClass());
                    throw new DocxStamperException(message, e);
                } else if (leaveEmptyOnExpressionError) {
                    log.warn(
                            "Expression {} could not be resolved against context root of type {}. Reason: {}. Set log level to TRACE to view Stacktrace.",
                            expression,
                            context.getClass(),
                            e.getMessage());
                    log.trace("Reason for skipping expression:", e);
                    paragraph.replace(expression, RunUtil.create(""));
                } else if (replaceUnresolvedExpressions) {
                    log.warn(
                            "Expression {} could not be resolved against context root of type {}. Reason: {}. Set log level to TRACE to view Stacktrace.",
                            expression,
                            context.getClass(),
                            e.getMessage());
                    log.trace("Reason for skipping expression:", e);
                    paragraph.replace(
                            expression,
                            RunUtil.create(unresolvedExpressionsDefaultValue));
                } else {
                    // DO NOTHING
                }
            }
        }
        if (lineBreakPlaceholder() != null) {
            replaceLineBreaks(paragraph);
        }
    }


    // TODO: Remove this intermediate method
    private Expression lineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    private void replaceLineBreaks(ParagraphWrapper paragraph) {
        Br lineBreak = Context.getWmlObjectFactory()
                .createBr();
        R run = RunUtil.create(lineBreak);
        while (paragraph.getText()
                .contains(lineBreakPlaceholder().inner())) {
            paragraph.replace(lineBreakPlaceholder(), run);
        }
    }
}
