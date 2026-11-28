package pro.verron.officestamper.api;

/// Post-processor that removes every comment and its markers from a document.
///
/// The concrete, library-specific traversal is supplied by a provider (R2-3).
public abstract class CommentRemover
        implements PostProcessor {
}
