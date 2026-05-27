package pro.verron.officestamper.asciidoc.core;

import org.jspecify.annotations.Nullable;

import java.util.List;

/// Table row.
///
/// @param cells table cells
public record Row(List<Cell> cells, @Nullable String style) {
    /// Constructor.
    ///
    /// @param cells table cells
    public Row(List<Cell> cells) {
        this(cells, null);
    }

    public Row(List<Cell> cells, @Nullable String style) {
        this.cells = List.copyOf(cells);
        this.style = style;
    }
}
