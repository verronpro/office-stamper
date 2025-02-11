package pro.verron.officestamper.preset.postprocessors.linebreaker;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.PostProcessor;
import pro.verron.officestamper.core.Placeholders;
import pro.verron.officestamper.core.TextualDocxPart;
import pro.verron.officestamper.utils.WmlFactory;

public record Linebreaker(String linebreakPlaceholder)
        implements PostProcessor {
    @Override
    public void process(WordprocessingMLPackage document) {
        var list = new TextualDocxPart(document).streamParagraphs()
                                                .toList();
        for (var paragraph : list) {
            if (paragraph.asString()
                         .contains(linebreakPlaceholder))
                paragraph.replace(Placeholders.raw(linebreakPlaceholder), WmlFactory.newBr());
        }
    }
}
