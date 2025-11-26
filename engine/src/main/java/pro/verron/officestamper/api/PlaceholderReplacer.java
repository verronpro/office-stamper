package pro.verron.officestamper.api;

public interface PlaceholderReplacer {
    Insert resolve(DocxPart docxPart, Tag tag, Object context);
}
