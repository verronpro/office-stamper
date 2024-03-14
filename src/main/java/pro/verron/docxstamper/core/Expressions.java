package pro.verron.docxstamper.core;

import org.wickedsource.docxstamper.api.DocxStamperException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for working with expressions in a text.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class Expressions {
    private static final Pattern PROC_PATTERN = Pattern.compile("#\\{(.*?)}");
    private static final Matcher PROC_MATCHER = new Matcher("#{", "}");
    private static final ExpressionFinder PROC_FINDER =
            new ExpressionFinder(PROC_PATTERN, PROC_MATCHER);
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.*?)}");
    private static final Matcher VAR_MATCHER = new Matcher("${", "}");
    private static final ExpressionFinder VAR_FINDER =
            new ExpressionFinder(VAR_PATTERN, VAR_MATCHER);

    private Expressions() {
        throw new DocxStamperException(
                "Utility classes should not be instantiated!");
    }

    public static List<Expression> findVariables(String text) {
        return VAR_FINDER.find(text);
    }

    public static List<Expression> findProcessors(String text) {
        return PROC_FINDER.find(text);
    }
}
