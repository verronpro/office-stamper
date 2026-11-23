package pro.verron.officestamper.api;

/// Pre-processor that wraps comment range starts with smart tags so the engine can process them later.
///
/// <p>The concrete, library-specific traversal is supplied by a provider (R2-3).
public abstract class CommentHooker
        implements PreProcessor {

    /// Default constructor.
    public CommentHooker() {
    }

    @Override
    public abstract void process(OfficeDocument document);
}
