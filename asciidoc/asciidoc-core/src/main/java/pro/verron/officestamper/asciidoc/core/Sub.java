package pro.verron.officestamper.asciidoc.core;

import java.util.List;

import static java.util.stream.Collectors.joining;

/// Represents a subscript inline element within an AsciiDoc document model.
///
/// This class implements the Inline interface and contains a list of child inlines.
/// It is used to represent text or elements that should appear as subscript.
///
/// The content of this inline element is immutable, and the provided child elements
/// are deep-copied to preserve immutability.
///
/// @param children the list of inline fragments contained within this subscript element
public record Sub(List<Inline> children)
        implements Inline {
    public Sub(List<Inline> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public String text() {
        return children.stream()
                       .map(Inline::text)
                       .collect(joining());
    }
}
