package pro.verron.officestamper.api;

public interface PlaceholderReplacer {
    Insert resolve(DocxPart docxPart, String expression, Object context);
}
