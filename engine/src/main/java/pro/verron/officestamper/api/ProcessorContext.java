package pro.verron.officestamper.api;

/// Represents a context for processing a placeholder within a specific paragraph and comment in a document.
/// This immutable record encapsulates essential components required to manipulate placeholders.
///
/// @param paragraph   The paragraph in which the placeholder exists.
/// @param comment     The comment associated with the placeholder.
/// @param placeholder The placeholder to be processed within the paragraph.
public record ProcessorContext(DocxPart part, Paragraph paragraph, Comment comment, Placeholder placeholder) {}
