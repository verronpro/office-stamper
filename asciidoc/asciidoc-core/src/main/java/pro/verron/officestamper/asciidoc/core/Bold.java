package pro.verron.officestamper.asciidoc.core;

import java.util.List;

import static java.util.stream.Collectors.joining;

/// Bold inline that can contain nested inlines.
///
/// @param children nested inline fragments
public record Bold(List<Inline> children)
        implements Inline {
    /// Constructor.
    ///
    /// @param children nested inline fragments
    public Bold(List<Inline> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public String text() {
        return children.stream()
                       .map(Inline::text)
                       .collect(joining());
    }
}
