package pro.verron.docxstamper.core;


import org.springframework.lang.NonNull;

/**
 * Represents a Matcher that checks if an expression starts with a specified prefix and ends with a specified suffix.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.5
 */
public record Matcher(
        @NonNull String prefix,
        @NonNull String suffix
) {
    @NonNull
    boolean match(@NonNull String expression) {
        return expression.startsWith(prefix)
               && expression.endsWith(suffix);
    }

    /**
     * Strips the prefix and suffix from the given expression.
     * @param expression the expression to strip.
     * @return the stripped expression.
     */
    @NonNull
    public String strip(@NonNull String expression) {
        int start = prefix.length();
        int end = expression.length() - suffix.length();
        return expression.substring(start, end);
    }
}
