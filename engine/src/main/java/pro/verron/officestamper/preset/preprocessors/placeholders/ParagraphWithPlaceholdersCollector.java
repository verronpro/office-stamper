package pro.verron.officestamper.preset.preprocessors.placeholders;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.WmlUtils.asString;

public class ParagraphWithPlaceholdersCollector
        extends TraversalUtilVisitor<P> {

    public final Pattern pattern;
    private final List<P> results = new ArrayList<>();

    public ParagraphWithPlaceholdersCollector(Pattern pattern) {
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

    public List<P> getParagraphWithPlaceholders() {
        return results;
    }
}
