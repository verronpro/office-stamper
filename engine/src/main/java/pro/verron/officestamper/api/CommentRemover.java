package pro.verron.officestamper.api;

/// Post-processor that removes every comment and its markers from a document.
///
/// <p>The concrete, library-specific traversal is supplied by a provider (R2-3).
public abstract class CommentRemover
        implements PostProcessor {

    /// Default constructor.
    public CommentRemover() {
    }

    @Override
    public abstract void process(OfficeDocument document);
}
