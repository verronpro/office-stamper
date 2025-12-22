package pro.verron.officestamper.preset.preprocessors.placeholders;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.core.DocumentUtil;

import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.wml.WmlUtils.asString;
import static pro.verron.officestamper.utils.wml.WmlUtils.insertSmartTag;

/// The [PrepareInlinePlaceholders] class is a pre-processor that prepares inline placeholders in a `WordprocessingML`
/// document. It searches for placeholders that match a given pattern and wraps them with a specified XML element to
/// ensure proper processing by the OfficeStamper engine.
///
/// This pre-processor is typically used to identify and mark inline expressions within paragraphs, making them
/// recognizable for subsequent processing steps.
public class PrepareInlinePlaceholders
        implements PreProcessor {

    private final Pattern pattern;
    private final String element;


    /// Constructs a new [PrepareInlinePlaceholders] instance with the specified regular expression and XML element
    /// name.
    ///
    /// @param regex the regular expression pattern used to identify inline placeholders in the document. This
    ///         pattern should contain at least two capturing groups where the second group represents the actual
    ///         placeholder content.
    /// @param element the name of the XML element to wrap around identified placeholders. This element will be
    ///         used to mark the placeholders for further processing.
    public PrepareInlinePlaceholders(String regex, String element) {
        this(Pattern.compile(regex, Pattern.DOTALL), element);
    }


    /// Constructs a new [PrepareInlinePlaceholders] instance with the specified pattern and XML element name.
    ///
    /// @param pattern the compiled regular expression pattern used to identify inline placeholders in the
    ///         document. This pattern should contain at least two capturing groups where the second group represents
    ///         the actual placeholder content.
    /// @param element the name of the XML element to wrap around identified placeholders. This element will be
    ///         used to mark the placeholders for further processing.
    public PrepareInlinePlaceholders(Pattern pattern, String element) {
        this.pattern = pattern;
        this.element = element;
    }

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new ParagraphCollector(pattern);
        DocumentUtil.visitDocument(document, visitor);
        for (var paragraph : visitor.paragraphs()) {
            var string = asString(paragraph);
            var matcher = pattern.matcher(string);
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
}
