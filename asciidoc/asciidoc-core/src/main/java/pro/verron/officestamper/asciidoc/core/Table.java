package pro.verron.officestamper.asciidoc.core;

import java.util.Collection;
import java.util.List;

/// Simple table block: list of rows; each row is a list of cells; each cell contains inline content.
///
/// @param rows table rows
public record Table(List<Row> rows)
        implements Block {
    /// Constructor.
    ///
    /// @param rows table rows
    public Table(List<Row> rows) {
        this.rows = List.copyOf(rows);
    }

    @Override
    public int size() {
        return rows.stream()
                   .map(Row::cells)
                   .flatMap(Collection::stream)
                   .map(Cell::blocks)
                   .flatMap(Collection::stream)
                   .mapToInt(Block::size)
                   .sum();
    }
}
