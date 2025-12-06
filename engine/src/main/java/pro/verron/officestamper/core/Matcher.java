package pro.verron.officestamper.core;


/// The Matcher class provides methods to match and strip expressions based on a specified prefix and suffix.
/// The match() method checks if an expression starts with the prefix
/// and ends with the suffix.
/// The strip() method removes the prefix and suffix from an expression
/// and returns the inner part.
public record Matcher(String prefix, String suffix) {

    /// Checks if the given expression matches the specified criteria.
    ///
    /// @param expression the expression to be matched.
    ///
    /// @return `true` if the expression starts with the prefix
    /// and ends with the suffix, `false` otherwise.
    public boolean match(String expression) {
        return expression.startsWith(prefix) && expression.endsWith(suffix);
    }

    /// Strips the prefix and suffix from the given expression and returns the inner part.
    ///
    /// @param expression the expression to be stripped.
    ///
    /// @return the inner part of the expression after stripping the prefix and suffix.
    public String strip(String expression) {
        int start = prefix.length();
        int end = expression.length() - suffix.length();
        return expression.substring(start, end);
    }
}
