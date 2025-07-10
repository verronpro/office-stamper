package pro.verron.officestamper.core;

import pro.verron.officestamper.api.Placeholder;

/**
 * The StandardPlaceholder class represents a placeholder with a specific
 * matching condition and associated expression.
 * It uses a {@link Matcher} to determine if the placeholder expression matches
 * a given prefix and suffix, and to extract the inner content of the placeholder.
 */
public record StandardPlaceholder(Matcher matcher, String expression)
        implements Placeholder {

    @Override
    public String content() {
        return matcher.match(expression) ? matcher.strip(expression) : expression;
    }

    @Override
    public String toString() {
        return "[%s]".formatted(expression);
    }
}
