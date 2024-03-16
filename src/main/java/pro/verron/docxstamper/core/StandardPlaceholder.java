package pro.verron.docxstamper.core;

import pro.verron.docxstamper.api.Placeholder;
import pro.verron.docxstamper.core.expression.Matcher;

/**
 * Represents an expression with a configured Matcher.
 */
public record StandardPlaceholder(
        Matcher matcher,
        String expression
)
        implements Placeholder {
    /**
     * Returns the inner part of the expression
     * by stripping the prefix and suffix.
     *
     * @return the inner part of the expression.
     */
    @Override
    public String content() {
        return matcher.match(expression)
                ? matcher.strip(expression)
                : expression;
    }
}
