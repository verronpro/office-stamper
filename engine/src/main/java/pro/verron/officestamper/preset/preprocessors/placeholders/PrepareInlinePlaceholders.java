package pro.verron.officestamper.preset.preprocessors.placeholders;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.core.DocumentUtil;

import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.WmlUtils.asString;
import static pro.verron.officestamper.utils.WmlUtils.insertSmartTag;

public class PrepareInlinePlaceholders
        implements PreProcessor {

    private final Pattern pattern;

    public PrepareInlinePlaceholders(String regex) {
        this(Pattern.compile(regex, Pattern.DOTALL));
    }

    public PrepareInlinePlaceholders(Pattern pattern) {
        this.pattern = pattern;
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
                var newContent = insertSmartTag(paragraph, placeholder, start, end);
                var content = paragraph.getContent();
                content.clear();
                content.addAll(newContent);
                string = asString(paragraph);
                matcher = pattern.matcher(string);
            }
        }
    }
}
