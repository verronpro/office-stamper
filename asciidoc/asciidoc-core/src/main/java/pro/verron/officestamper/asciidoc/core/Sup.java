package pro.verron.officestamper.asciidoc.core;

import java.util.List;

import static java.util.stream.Collectors.joining;

/// Represents a superscript inline fragment in an AsciiDoc document.
///
/// This class encapsulates a list of child [Inline] elements and provides a method to return
/// the concatenated text content of all child elements. It is an immutable record type, providing
/// safety and ensuring that the children list cannot be externally modified after the instance
/// is created.
public record Sup(List<Inline> children)
        implements Inline {
    /// Constructs a [Sup] instance, representing a superscript inline fragment in an AsciiDoc document.
    ///
    /// The Sup instance encapsulates a list of [Inline] child elements. The list is copied to ensure immutability,
    /// providing safety and preventing external modification after creation.
    ///
    /// @param children the list of [Inline] elements to be included as children of the superscript fragment
    ///                 (must not be null; each element should represent a valid inline fragment)
    public Sup(List<Inline> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public String text() {
        return children.stream()
                       .map(Inline::text)
                       .collect(joining());
    }
}
