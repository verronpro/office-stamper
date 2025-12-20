package pro.verron.officestamper.preset.preprocessors.placeholders;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.wml.WmlUtils.asString;

/// A [TraversalUtilVisitor] implementation that collects paragraphs matching a given pattern.
///
/// This class is used to traverse a document and collect all paragraph elements ([P]) that match a specified regular
/// expression pattern. The collected paragraphs can be retrieved using the [#paragraphs()] method.
public class ParagraphCollector
        extends TraversalUtilVisitor<P> {

    private final Pattern pattern;
    private final List<P> results = new ArrayList<>();


    /// Constructs a new [ParagraphCollector] with the specified pattern.
    ///
    /// @param pattern the regular expression pattern to match against paragraphs
    public ParagraphCollector(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public void apply(P element) {
        var matcher = pattern.asPredicate();
        var string = asString(element);
        if (matcher.test(string)) {
            results.add(element);
        }
    }


    /// Returns the list of collected paragraphs that matched the pattern.
    ///
    /// @return an unmodifiable list of paragraphs matching the specified pattern
    public List<P> paragraphs() {
        return results;
    }
}
