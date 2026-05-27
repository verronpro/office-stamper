package pro.verron.officestamper.asciidoc.core;

import java.util.List;

/// Italic inline that can contain nested inlines.
///
/// @param children nested inline fragments
public record Italic(List<Inline> children)
        implements Inline {
    /// Constructor.
    ///
    /// @param children nested inline fragments
    public Italic(List<Inline> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public String text() {
        StringBuilder sb = new StringBuilder();
        for (Inline in : children) sb.append(in.text());
        return sb.toString();
    }
}
