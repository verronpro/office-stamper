package pro.verron.docxstamper.core;

import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.util.ParagraphWrapper;
import pro.verron.docxstamper.core.expression.ExpressionFinder;
import pro.verron.docxstamper.core.expression.Matcher;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The Expressions class provides utility methods for finding variables and processors in a given text.
 * It contains multiple constant variables for different types of expressions, such as VAR_MATCHER for variable expressions and PROC_MATCHER for processor expressions.
 * The findVariables() method uses VAR_FINDER to find variable expressions in a given text and returns a list of found expressions.
 * The findProcessors() method uses PROC_FINDER to find processor expressions in a given text and returns a list of found expressions.
 * The raw() method creates a new Expression object using the RAW_MATCHER and a specified text.
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
    /**
     * A Matcher matching raw expressions.
     * It is typically used to wrap raw expressions that do not have a
     * specific prefix or suffix.
     */
    private static final Matcher RAW_MATCHER = new Matcher("", "");

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
    public static List<Expression> findVariables(String text) {
        return VAR_FINDER.find(text);
    }

    public static List<Expression> findVariables(ParagraphWrapper paragraph) {
        return findVariables(paragraph.getText());
    }

    /**
     * Finds processors expressions in a given text.
     *
     * @param text the text to search for processor expressions
     * @return a list of found processor expressions as {@link Expression}
     * objects
     */
    public static List<Expression> findProcessors(String text) {
        return PROC_FINDER.find(text);
    }

    public static Expression raw(String text) {
        return new Expression(RAW_MATCHER, text);
    }
}
