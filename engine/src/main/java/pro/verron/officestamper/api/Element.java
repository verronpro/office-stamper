package pro.verron.officestamper.api;

import java.util.List;
import org.jspecify.annotations.Nullable;

/// A node in a document's content tree. The underlying representation is hidden
/// behind this interface so that consumers never depend on a specific OOXML
/// library.
///
/// [#text()] returns the *canonical textual rendering* of the element
/// (tabs as `\t`, breaks as `\n`, field instructions as `<instrText>` placeholders).
/// The engine relies on this rendering being
/// consistent across reads, since placeholder offsets are computed against it.
public interface Element {

    /// @return the direct child elements of this element
    List<Element> children();

    /// @return a live, mutable view of this element's content when it is a container element;
    ///         for leaf elements the view is empty and its mutators throw
    ///         [UnsupportedOperationException]
    Content content();

    /// @return the parent element of this element, or `null` at the root of the tree
    @Nullable Element parent();

    /// @return a deep copy of this element, detached from any parent (re-parented when inserted
    ///         through [Content#addAll(int, List)])
    Element copy();

    /// @return the textual content of this element (e.g. a run's text)
    String text();

    /// Replaces the textual content of this element.
    ///
    /// @param text the new text
    void setText(String text);

    /// @return `true` if this element represents a text run
    boolean isRun();

    /// @return the formatting properties of this element when it is a text run, or of the first text
    ///         run found within it when it is a container, or `null` when there is none
    @Nullable RunProperties runProperties();
}
