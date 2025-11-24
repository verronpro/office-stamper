package pro.verron.officestamper.api;

public interface ParagraphPlaceholderReplacer {
    void resolveExpressionsForParagraph(DocxPart docxPart, Tag tag, Object context);
}
