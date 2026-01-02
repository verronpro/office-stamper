package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.wml.WmlUtils.asString;
import static pro.verron.officestamper.utils.wml.WmlUtils.insertSmartTag;

/// The [PlaceholderHooker] class is a pre-processor that prepares inline placeholders in a `WordprocessingML`
/// document. It searches for placeholders that match a given pattern and wraps them with a specified XML element to
/// ensure proper processing by the OfficeStamper engine.
///
/// This pre-processor is typically used to identify and mark inline expressions within paragraphs, making them
/// recognizable for subsequent processing steps.
public class PlaceholderHooker
        implements PreProcessor {

    private final Pattern pattern;
    private final String element;


    /// Constructs a new [PlaceholderHooker] instance with the specified regular expression and XML element
    /// name.
    ///
    /// @param regex the regular expression pattern used to identify inline placeholders in the document. This
    ///         pattern should contain at least two capturing groups where the second group represents the actual
    ///         placeholder content.
    /// @param element the name of the XML element to wrap around identified placeholders. This element will be
    ///         used to mark the placeholders for further processing.
    public PlaceholderHooker(String regex, String element) {
        this(Pattern.compile(regex, Pattern.DOTALL), element);
    }


    /// Constructs a new [PlaceholderHooker] instance with the specified pattern and XML element name.
    ///
    /// @param pattern the compiled regular expression pattern used to identify inline placeholders in the
    ///         document. This pattern should contain at least two capturing groups where the second group represents
    ///         the actual placeholder content.
    /// @param element the name of the XML element to wrap around identified placeholders. This element will be
    ///         used to mark the placeholders for further processing.
    public PlaceholderHooker(Pattern pattern, String element) {
        this.pattern = pattern;
        this.element = element;
    }

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new ParagraphCollector(pattern);
        WmlUtils.visitDocument(document, visitor);
        for (var paragraph : visitor.paragraphs()) {
            var string = asString(paragraph);
            var matcher = pattern.matcher(string);
            // Iterates matches; replaces placeholder with a smart tag
            while (matcher.find()) {
                var start = matcher.start(1);
                var end = matcher.end(1);
                var placeholder = matcher.group(2);
                var newContent = insertSmartTag(element, paragraph, placeholder, start, end);
                var content = paragraph.getContent();
                content.clear();
                content.addAll(newContent);
                string = asString(paragraph);
                matcher = pattern.matcher(string);
            }
        }
    }

    /// A [TraversalUtilVisitor] implementation that collects paragraphs matching a given pattern.
    ///
    /// This class is used to traverse a document and collect all paragraph elements ([P]) that match a specified
    /// regular expression pattern. The collected paragraphs can be retrieved using the [#paragraphs()] method.
    public static class ParagraphCollector
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
}
