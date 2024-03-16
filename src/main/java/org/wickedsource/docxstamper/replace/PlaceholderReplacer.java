package org.wickedsource.docxstamper.replace;

import org.wickedsource.docxstamper.el.ExpressionResolver;
import pro.verron.docxstamper.api.Placeholder;
import pro.verron.docxstamper.core.ObjectResolverRegistry;

/**
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the {@link pro.verron.docxstamper.core.PlaceholderReplacer} class instead.
 * This class will not be exported in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public class PlaceholderReplacer
        extends pro.verron.docxstamper.core.PlaceholderReplacer {
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
     * @param linebreakPlaceholder              if set to a non-null value,
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
            Placeholder linebreakPlaceholder
    ) {
        super(registry,
              resolver,
              failOnUnresolvedExpression,
              replaceUnresolvedExpressions,
              unresolvedExpressionsDefaultValue,
              leaveEmptyOnExpressionError,
              linebreakPlaceholder);
    }
}
