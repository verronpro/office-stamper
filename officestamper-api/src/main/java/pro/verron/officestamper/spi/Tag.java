package pro.verron.officestamper.spi;

/// A hook tag anchored in a paragraph: the mechanism the engine uses to mark placeholders and
/// comment processors inside a document, and to track their execution state through attributes.
///
/// <p>Tags are discovered with [Hooks] and consumed by the engine's hook orchestration.
public interface Tag
        extends Element {

    /// @return the value of the {@code type} attribute, or {@code null} when absent
    String type();

    /// @param name the attribute name
    /// @return the value of the named attribute, or {@code null} when absent
    String attribute(String name);

    /// Sets or updates the named attribute, creating the attribute list lazily.
    ///
    /// @param name  the attribute name
    /// @param value the attribute value
    void setAttribute(String name, String value);

    /// Replaces this tag's content with the given insert's elements. When the insert carries no
    /// [RunProperties], the formatting of this tag's first text run is applied to it, preserving the
    /// placeholder's original style.
    ///
    /// @param insert the elements to install as this tag's content
    void replace(Insert insert);

    /// Removes this tag — and its content — from its parent.
    void remove();

    /// Replaces this tag with its own content in its parent: the tag disappears but its content is
    /// kept. A no-op when the tag is no longer attached to its parent (a shared part may have been
    /// inlined by a previous pass).
    void inline();
}
