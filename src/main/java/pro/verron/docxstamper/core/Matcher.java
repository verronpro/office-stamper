package pro.verron.docxstamper.core;


import org.springframework.lang.NonNull;

/**
 * The Matcher class represents a matching criteria for expressions.
 * It defines a prefix and suffix,
 * and provides methods to match and strip expressions.
 */
public record Matcher(
        @NonNull String prefix,
        @NonNull String suffix
) {

    /**
     * Checks if the given expression matches the specified criteria.
     *
     * @param expression the expression to be matched.
     * @return {@code true} if the expression starts with the prefix
     * and ends with the suffix,
     * {@code false} otherwise.
     */
    @NonNull
    boolean match(@NonNull String expression) {
        return expression.startsWith(prefix)
               && expression.endsWith(suffix);
    }

    /**
     * Strips the prefix and suffix from the given expression and returns the inner part.
     *
     * @param expression the expression to be stripped.
     * @return the inner part of the expression after stripping the prefix and suffix.
     */
    @NonNull
    String strip(@NonNull String expression) {
        int start = prefix.length();
        int end = expression.length() - suffix.length();
        return expression.substring(start, end);
    }
}
