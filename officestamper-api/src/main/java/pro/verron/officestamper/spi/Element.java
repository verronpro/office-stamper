package pro.verron.officestamper.spi;

import java.util.List;

/// A node in a document's content tree. The underlying representation is hidden
/// behind this interface so that consumers never depend on a specific OOXML
/// library.
public interface Element {

    /// @return the direct child elements of this element
    List<Element> children();

    /// @return the textual content of this element (e.g. a run's text)
    String text();

    /// Replaces the textual content of this element.
    ///
    /// @param text the new text
    void setText(String text);

    /// @return {@code true} if this element represents a text run
    boolean isRun();

    /// @return the formatting properties of this element when it is a text run carrying run
    ///         properties, or {@code null} otherwise
    RunProperties runProperties();
}
