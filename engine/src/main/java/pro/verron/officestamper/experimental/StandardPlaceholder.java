package pro.verron.officestamper.experimental;

import pro.verron.officestamper.core.Matcher;

/// The StandardPlaceholder class represents a placeholder with a specific
/// matching condition and associated expression.
/// It uses a [Matcher] to determine if the placeholder expression matches
/// a given prefix and suffix, and to extract the inner content of the placeholder.
///
/// @param matcher    the matcher to use for determining if the expression matches.
/// @param expression the expression string.
public record StandardPlaceholder(Matcher matcher, String expression)
        implements Placeholder {

    /// Retrieves the processed content of the placeholder. If the expression matches
    /// the criteria defined by the `Matcher`, the prefix and suffix are stripped from
    /// the expression. Otherwise, the original expression is returned.
    ///
    /// @return the inner content of the expression if the match criteria are met;
    /// otherwise, the complete original expression.
    @Override
    public String content() {
        return matcher.match(expression) ? matcher.strip(expression) : expression;
    }

    /// Returns a string representation of this object.
    /// The representation is enclosed in square brackets and includes the expression.
    ///
    /// @return a string in the format "\[expression]" where "expression" is the value of the expression field.
    @Override
    public String toString() {
        return "[%s]".formatted(expression);
    }
}
