package pro.verron.officestamper.asciidoc.core;

import java.util.List;

/// List item.
///
/// @param inlines inline fragments
public record ListItem(List<Inline> inlines) {
    /// Constructor.
    ///
    /// @param inlines inline fragments
    public ListItem(List<Inline> inlines) {
        this.inlines = List.copyOf(inlines);
    }
}
