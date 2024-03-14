package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.util.ParagraphWrapper;

public interface ParagraphPlaceholderReplacer {
    void resolveExpressionsForParagraph(
            ParagraphWrapper paragraph,
            Object context,
            WordprocessingMLPackage document
    );
}
