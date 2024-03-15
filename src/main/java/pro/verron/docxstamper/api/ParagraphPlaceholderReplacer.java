package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public interface ParagraphPlaceholderReplacer {
    void resolveExpressionsForParagraph(
            Paragraph paragraph,
            Object context,
            WordprocessingMLPackage document
    );
}
