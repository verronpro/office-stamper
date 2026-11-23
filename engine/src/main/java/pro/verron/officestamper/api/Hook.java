package pro.verron.officestamper.api;

/// A hook that extends or customizes the behaviour of a document-processing flow.
///
/// <p>Implementations execute logic within a {@link ProcessorContext}, for example to process
/// smart tags or comments, or to attach custom behaviour during rendering.
public interface Hook {

    /// Sets the context key associated with this hook. The key scopes the hook's execution within a
    /// specific {@link ContextTree} branch during document processing.
    ///
    /// @param contextKey the unique key representing the context in which this hook operates
    void setContextKey(String contextKey);
}
