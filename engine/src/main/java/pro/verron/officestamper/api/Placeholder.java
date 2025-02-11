package pro.verron.officestamper.api;

/// The Placeholder interface represents a placeholder in a text document.
/// It provides methods to retrieve the content of the placeholder and the full expression.
public interface Placeholder {

    /// Returns the content of the placeholder.
    ///
    /// @return the content of the placeholder
    String content();

    /// Retrieves the expression of the placeholder.
    ///
    /// @return the expression of the placeholder.
    String expression();
}
