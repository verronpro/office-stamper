package pro.verron.officestamper.api;

/// Represents a hook that can be used to extend or customize the behavior of an OfficeStamper document processing flow.
/// Implementations of this interface provide a mechanism to execute specific logic within the context of a document. A
/// Hook can be used, for example, to process specific elements within a document structure (like smart tags or
/// comments), attach custom logic, or handle context-specific processing during document rendering.
public interface Hook {

    /// Sets the context key associated with this hook. The context key is a unique identifier
    /// that can be used to match or scope the hook's execution within a specific [ContextTree] branch
    /// during document processing.
    ///
    /// @param contextKey the unique key representing the context in which this hook should operate
    void setContextKey(String contextKey);
}
