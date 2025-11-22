package pro.verron.officestamper.preset.preprocessors.placeholders;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.core.DocumentUtil;


import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.WmlUtils.asString;
import static pro.verron.officestamper.utils.WmlUtils.insertSmartTag;

public class PrepareInlineProcessors
        implements PreProcessor {
    @Override
    public void process(WordprocessingMLPackage document) {
        var pattern = Pattern.compile("(#\\{([^{]+?)})", Pattern.DOTALL);
        var visitor = new ParagraphCollector(pattern);
        DocumentUtil.visitDocument(document, visitor);
        for (P paragraphWithPlaceholders : visitor.getParagraphWithPlaceholders()) {
            var string = asString(paragraphWithPlaceholders);
            var matcher = pattern.matcher(string);
            while (matcher.find()) {
                var start = matcher.start(1);
                var end = matcher.end(1);
                var placeholder = matcher.group(2);
                var newContent = insertSmartTag(paragraphWithPlaceholders, placeholder, start, end);
                paragraphWithPlaceholders.getContent().clear();
                paragraphWithPlaceholders.getContent().addAll(newContent);
                string = asString(paragraphWithPlaceholders);
                matcher = pattern.matcher(string);
            }
        }
    }
}
