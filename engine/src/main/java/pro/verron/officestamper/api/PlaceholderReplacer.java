package pro.verron.officestamper.api;

public interface PlaceholderReplacer {
    Insert resolve(DocxPart docxPart, Placeholder placeholder, Object context);
}
