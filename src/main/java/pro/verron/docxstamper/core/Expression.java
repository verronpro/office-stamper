package pro.verron.docxstamper.core;

import org.springframework.lang.NonNull;

import java.util.Objects;

public final class Expression {
    private static final Matcher DEFAULT_MATCHER = new Matcher("${", "}");
    private static final Matcher SECONDARY_MATCHER = new Matcher("#{", "}");
    @NonNull
    private final String expression;

    public Expression(@NonNull String expression) {
        this.expression = expression;
    }

    /**
     * Cleans the given expression by stripping the prefix and suffix if they match any of the configured matchers.
     *
     * @return the cleaned expression.
     */
    public String inner() {
        if (DEFAULT_MATCHER.match(expression))
            return DEFAULT_MATCHER.strip(expression);
        if (SECONDARY_MATCHER.match(expression))
            return SECONDARY_MATCHER.strip(expression);
        return expression;
    }

    @NonNull
    public String expression() {return expression;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Expression) obj;
        return Objects.equals(this.expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return "Expression[" +
               "expression=" + expression + ']';
    }


}
