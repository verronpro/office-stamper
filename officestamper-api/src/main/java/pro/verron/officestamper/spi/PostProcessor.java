package pro.verron.officestamper.spi;

/// A processor applied to a document after stamping.
///
/// <p>Implementations may clean up the document structure (e.g. remove orphaned comments or
/// smart tags). The concrete, library-specific behavior is supplied by a provider.
public interface PostProcessor {

    /// Processes the given document.
    ///
    /// @param document the document to process
    void process(OfficeDocument document);
}
