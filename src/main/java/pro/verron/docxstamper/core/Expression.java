package pro.verron.docxstamper.core;

import org.springframework.lang.NonNull;

/**
 * Represents an expression with a configured Matcher.
 */
public record Expression(
        @NonNull Matcher matcher,
        @NonNull String expression
) {
    /**
     * Returns the inner part of the expression
     * by stripping the prefix and suffix.
     *
     * @return the inner part of the expression.
     */
    public String inner() {
        return matcher.match(expression)
                ? matcher.strip(expression)
                : expression;
    }
}
