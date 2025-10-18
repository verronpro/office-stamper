package pro.verron.officestamper.api;

import org.docx4j.wml.R;

/// Represents the context in which a processor operates in a text document.
/// Contains information about the paragraph, run, comment, and placeholder being processed.
///
/// @param paragraph   The paragraph associated with this context.
/// @param run         The run object representing a run of text.
/// @param comment     The comment associated with this context.
/// @param placeholder The placeholder being processed in this context.
public record ProcessorContext(Paragraph paragraph, R run, Comment comment, Placeholder placeholder) {}
