package pro.verron.officestamper.api;

/// Context for a document-processing operation, exposed without leaking the underlying OOXML library.
///
/// The base context bundles the part being processed, the expression under evaluation and the context
/// tree that manages hierarchical scopes. Paragraph-aware operations use
/// [ParagraphProcessorContext]; comment-anchored operations use
/// [CommentProcessorContext].
public class ProcessorContext {

    private final OfficePart part;
    private final String expression;
    private final ContextTree contextTree;

    /// Constructs a processor context.
    ///
    /// @param part        the part being processed
    /// @param expression  the expression being evaluated
    /// @param contextTree the context tree managing scopes
    public ProcessorContext(OfficePart part, String expression, ContextTree contextTree) {
        this.part = part;
        this.expression = expression;
        this.contextTree = contextTree;
    }

    /// @return the part being processed
    public OfficePart part() {
        return part;
    }

    /// @return the expression being evaluated
    public String expression() {
        return expression;
    }

    /// @return the context tree managing scopes for this context
    public ContextTree contextHolder() {
        return contextTree;
    }
}
