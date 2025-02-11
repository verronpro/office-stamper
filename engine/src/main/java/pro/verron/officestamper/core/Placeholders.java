package pro.verron.officestamper.core;

import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Placeholder;

import java.util.List;
import java.util.regex.Pattern;

/// The Expressions class provides utility methods for finding variables and processors in a given text.
/// It contains multiple constant variables for different types of expressions, such as VAR_MATCHER for variable
/// expressions and PROC_MATCHER for processor expressions.
/// The findVariables() method uses VAR_FINDER to find variable expressions in a given text and returns a list of found
/// expressions.
/// The findProcessors() method uses PROC_FINDER to find processor expressions in a given text and returns a list of
/// found expressions.
/// The raw() method creates a new Expression object using the RAW_MATCHER and a specified text.
public class Placeholders {

    /// A regular expression pattern matching processor expressions.
    /// The pattern search for expressions starting with '#{' and ending with
    /// '}'.
    private static final Pattern PROC_PATTERN = Pattern.compile("[#$]\\{(.*?)}", Pattern.DOTALL);

    /// A Matcher matching processor expressions.
    /// The matcher checks for expressions starting with '#{' and ending with
    /// '}'.
    private static final Matcher PROC_MATCHER = new Matcher("[#$]\\{", "\\}");

    /// An ExpressionFinder to find processor expressions.
    /// It is initialized with a specified pattern and matcher.
    public static final PlaceholderFinder PROC_FINDER = new PlaceholderFinder(PROC_PATTERN, PROC_MATCHER);

    /// A regular expression pattern matching processor expressions.
    /// The pattern search for expressions starting with '${' and ending with
    /// '}'.
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.*?)}", Pattern.DOTALL);

    /// A Matcher matching processor expressions.
    /// The matcher checks for expressions starting with '${' and ending with
    /// '}'.
    private static final Matcher VAR_MATCHER = new Matcher("\\$\\{", "}");

    /// An ExpressionFinder to find variable expressions.
    /// It is initialized with a specified pattern and matcher.
    private static final PlaceholderFinder VAR_FINDER = new PlaceholderFinder(VAR_PATTERN, VAR_MATCHER);

    /// A Matcher matching raw expressions.
    /// It is typically used to wrap raw expressions that do not have a
    /// specific prefix or suffix.
    private static final Matcher RAW_MATCHER = new Matcher("", "");

    private Placeholders() {
        throw new OfficeStamperException("Utility classes should not be instantiated!");
    }

    /// Finds variable expressions in a given paragraph.
    ///
    /// @param paragraph the paragraph in which to search for variable expressions
    ///
    /// @return a list of found variable expressions as [Placeholder] objects
    public static List<Placeholder> findVariables(Paragraph paragraph) {
        return findVariables(paragraph.asString());
    }

    /// Finds variable expressions in a given text.
    ///
    /// @param text the text to search for variable expressions
    ///
    /// @return a list of found variable expressions as [StandardPlaceholder] objects
    public static List<Placeholder> findVariables(String text) {
        return VAR_FINDER.find(text);
    }

    /// Finds processors expressions in a given text.
    ///
    /// @param text the text to search for processor expressions
    ///
    /// @return a list of found processor expressions as [StandardPlaceholder]
    /// objects
    public static List<Placeholder> findProcessors(String text) {
        return PROC_FINDER.find(text);
    }

    /// Creates a new raw placeholder with the given text.
    ///
    /// @param text the text to be used as the content of the placeholder
    ///
    /// @return a new raw placeholder
    public static Placeholder raw(String text) {
        return new StandardPlaceholder(RAW_MATCHER, text);
    }
}
