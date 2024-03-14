package pro.verron.docxstamper.core;

import org.springframework.lang.NonNull;
import org.wickedsource.docxstamper.api.DocxStamperException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The Expressions class provides utility methods
 * for finding variables and processors in a given text.
 */
public class Expressions {
    /**
     * A regular expression pattern matching processor expressions.
     * The pattern search for expressions starting with '#{' and ending with
     * '}'.
     */
    private static final Pattern PROC_PATTERN = Pattern.compile("#\\{(.*?)}");
    /**
     * A Matcher matching processor expressions.
     * The matcher checks for expressions starting with '#{' and ending with
     * '}'.
     */
    private static final Matcher PROC_MATCHER = new Matcher("#{", "}");
    /**
     * An ExpressionFinder to find processor expressions.
     * It is initialized with a specified pattern and matcher.
     */
    private static final ExpressionFinder PROC_FINDER =
            new ExpressionFinder(PROC_PATTERN, PROC_MATCHER);

    /**
     * A regular expression pattern matching processor expressions.
     * The pattern search for expressions starting with '${' and ending with
     * '}'.
     */
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.*?)}");
    /**
     * A Matcher matching processor expressions.
     * The matcher checks for expressions starting with '${' and ending with
     * '}'.
     */
    private static final Matcher VAR_MATCHER = new Matcher("${", "}");
    /**
     * An ExpressionFinder to find variable expressions.
     * It is initialized with a specified pattern and matcher.
     */
    private static final ExpressionFinder VAR_FINDER =
            new ExpressionFinder(VAR_PATTERN, VAR_MATCHER);

    private Expressions() {
        throw new DocxStamperException(
                "Utility classes should not be instantiated!");
    }

    /**
     * Finds variable expressions in a given text.
     *
     * @param text the text to search for variable expressions
     * @return a list of found variable expressions as {@link Expression} objects
     */
    @NonNull
    public static List<Expression> findVariables(@NonNull String text) {
        return VAR_FINDER.find(text);
    }

    /**
     * Finds processors expressions in a given text.
     *
     * @param text the text to search for processor expressions
     * @return a list of found processor expressions as {@link Expression}
     * objects
     */
    @NonNull
    public static List<Expression> findProcessors(@NonNull String text) {
        return PROC_FINDER.find(text);
    }
}
