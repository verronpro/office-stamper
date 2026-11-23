package pro.verron.officestamper.api;

/// A processor applied to a document before stamping.
///
/// <p>Implementations may modify, sanitize, or transform the document content. The concrete,
/// library-specific behavior is supplied by a provider.
public interface PreProcessor {

    /// Processes the given document.
    ///
    /// @param document the document to process
    void process(OfficeDocument document);
}
