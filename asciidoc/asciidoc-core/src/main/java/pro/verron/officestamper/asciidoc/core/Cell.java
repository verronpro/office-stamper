package pro.verron.officestamper.asciidoc.core;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/// Table cell.
///

public class Cell {
    public final @Nullable String style;
    private final List<Block> blocks;

    /// @param blocks cell content blocks
    public Cell(List<Block> blocks) {
        this(blocks, null);
    }

    /// Constructor.
    ///
    /// @param blocks cell content blocks
    public Cell(List<Block> blocks, @Nullable String style) {
        this.blocks = List.copyOf(blocks);
        this.style = style;
    }

    /// Creates a new [Cell] instance by wrapping a list of [Inline] elements
    /// into a [Paragraph] and adding it to the cell's content blocks.
    ///
    /// @param inlines the list of [Inline] elements to be wrapped into a [Paragraph]
    /// @return a [Cell] containing the specified [Inline] elements as a single [Paragraph]
    public static Cell ofInlines(List<Inline> inlines) {
        return new Cell(List.of(new Paragraph(inlines)));
    }

    public List<Block> blocks() {
        return blocks;
    }

    public Optional<String> style() {
        return Optional.ofNullable(style);
    }
}
