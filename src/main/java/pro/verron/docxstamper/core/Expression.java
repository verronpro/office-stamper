package pro.verron.docxstamper.core;

import org.springframework.lang.NonNull;

public record Expression(
        @NonNull Matcher matcher,
        @NonNull String expression
) {
    /**
     * Cleans the given expression by stripping the prefix and suffix if they match any of the configured matchers.
     *
     * @return the cleaned expression.
     */
    public String inner() {
        return matcher.match(expression)
                ? matcher.strip(expression)
                : expression;
    }
}
