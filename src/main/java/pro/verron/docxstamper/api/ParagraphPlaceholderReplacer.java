package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.docxstamper.core.Paragraph;

public interface ParagraphPlaceholderReplacer {
    void resolveExpressionsForParagraph(
            Paragraph paragraph,
            Object context,
            WordprocessingMLPackage document
    );
}
