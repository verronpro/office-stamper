package pro.verron.officestamper.api;

/// Abstract base class for processing comments within a paragraph.
///
/// The CommentProcessor represents a mechanism to manipulate or interpret
/// comments and associated content such as placeholders found within a
/// paragraph structure.
///
/// Subclasses must implement specific processing logic.
public abstract class CommentProcessor {

    /// The processing context for this CommentProcessor instance.
    private final ProcessorContext context;
    /// The replacer used to replace placeholder expressions in paragraphs.
    private final PlaceholderReplacer replacer;

    /// Constructs a new instance of CommentProcessor to process comments and placeholders
    /// within a paragraph.
    ///
    /// @param context             the context containing the paragraph, comment, and placeholder
    ///                                           associated with the processing of this CommentProcessor.
    /// @param placeholderReplacer an implementation of PlaceholderReplacer used to
    ///                                                        resolve and replace placeholders in the paragraph.
    protected CommentProcessor(ProcessorContext context, PlaceholderReplacer placeholderReplacer) {
        this.context = context;
        this.replacer = placeholderReplacer;
    }

    protected Paragraph paragraph() {
        return context.paragraph();
    }

    protected Comment comment() {
        return context.comment();
    }

    protected PlaceholderReplacer replacer() {
        return replacer;
    }
}
