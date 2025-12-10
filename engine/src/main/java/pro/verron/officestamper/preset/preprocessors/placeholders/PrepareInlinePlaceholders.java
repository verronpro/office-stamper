package pro.verron.officestamper.preset.preprocessors.placeholders;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.core.DocumentUtil;

import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.wml.WmlUtils.asString;
import static pro.verron.officestamper.utils.wml.WmlUtils.insertSmartTag;

public class PrepareInlinePlaceholders
        implements PreProcessor {

    private final Pattern pattern;
    private final String element;

    public PrepareInlinePlaceholders(String regex, String element) {
        this(Pattern.compile(regex, Pattern.DOTALL), element);
    }

    public PrepareInlinePlaceholders(Pattern pattern, String element) {
        this.pattern = pattern;
        this.element = element;
    }

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new ParagraphCollector(pattern);
        DocumentUtil.visitDocument(document, visitor);
        for (P paragraph : visitor.paragraphs()) {
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
