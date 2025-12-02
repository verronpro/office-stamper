package pro.verron.officestamper.api;

/// Abstract base class for processing comments within a paragraph.
///
/// The CommentProcessor represents a mechanism to manipulate or interpret comments and associated content such as
/// placeholders found within a paragraph structure.
///
/// Subclasses must implement specific processing logic.
public abstract class CommentProcessor {

    /// The processing context for this CommentProcessor instance.
    private final ProcessorContext context;

    /// Constructs a new instance of CommentProcessor to process comments and placeholders within a paragraph.
    ///
    /// @param context the context containing the paragraph, comment, and placeholder associated with the
    ///         processing of this CommentProcessor.
    protected CommentProcessor(ProcessorContext context) {
        this.context = context;
    }

    protected ProcessorContext context() {
        return context;
    }

    protected Paragraph paragraph() {
        return context.paragraph();
    }

    protected Comment comment() {
        return context.comment();
    }
}
