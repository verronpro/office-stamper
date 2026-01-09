package pro.verron.officestamper.asciidoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Represents a minimal in-memory model of an AsciiDoc document.
///
/// This model intentionally supports a compact subset sufficient for rendering to WordprocessingML and JavaFX Scene: -
/// Headings (levels 1..6) using leading '=' markers - Paragraphs separated by blank lines - Inline emphasis for bold
/// and italic using AsciiDoc-like markers: *bold*, _italic_
public final class AsciiDocModel {
    private final List<Block> blocks;

    private AsciiDocModel(List<Block> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    /// Creates a new {@link AsciiDocModel} from the provided blocks.
    ///
    /// @param blocks ordered content blocks
    ///
    /// @return immutable AsciiDocModel
    public static AsciiDocModel of(List<Block> blocks) {
        Objects.requireNonNull(blocks, "blocks");
        return new AsciiDocModel(new ArrayList<>(blocks));
    }

    /// Returns the ordered list of blocks comprising the document.
    ///
    /// @return immutable list of blocks
    public List<Block> getBlocks() {
        return blocks;
    }

    /// Marker interface for document blocks.
    public sealed interface Block
            permits Heading, Paragraph, Table {}

    /// Inline fragment inside a paragraph/heading.
    public sealed interface Inline
            permits Text, Bold, Italic, Tab {
        /// Returns the text of the inline fragment.
        ///
        /// @return text
        String text();
    }

    /// Heading block (levels 1..6).
    ///
    /// @param level heading level
    /// @param inlines inline fragments
    public record Heading(int level, List<Inline> inlines)
            implements Block {
        /// Constructor.
        ///
        /// @param level heading level
        /// @param inlines inline fragments
        public Heading(int level, List<Inline> inlines) {
            if (level < 1 || level > 6) {
                throw new IllegalArgumentException("Heading level must be between 1 and 6");
            }
            this.level = level;
            this.inlines = List.copyOf(inlines);
        }
    }

    /// Paragraph block.
    ///
    /// @param inlines inline fragments
    public record Paragraph(List<Inline> inlines)
            implements Block {
        /// Constructor.
        ///
        /// @param inlines inline fragments
        public Paragraph(List<Inline> inlines) {
            this.inlines = List.copyOf(inlines);
        }
    }

    /// Text fragment.
    ///
    /// @param text text
    public record Text(String text)
            implements Inline {
    }

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
            StringBuilder sb = new StringBuilder();
            for (Inline in : children) sb.append(in.text());
            return sb.toString();
        }
    }

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

    /// Inline tab marker to be rendered as a DOCX tab stop.
    public record Tab()
            implements Inline {
        @Override
        public String text() {
            return "\t";
        }
    }

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
    }

    /// Table row.
    ///
    /// @param cells table cells
    public record Row(List<Cell> cells) {
        /// Constructor.
        ///
        /// @param cells table cells
        public Row(List<Cell> cells) {
            this.cells = List.copyOf(cells);
        }
    }

    /// Table cell.
    ///
    /// @param inlines inline fragments
    public record Cell(List<Inline> inlines) {
        /// Constructor.
        ///
        /// @param inlines inline fragments
        public Cell(List<Inline> inlines) {
            this.inlines = List.copyOf(inlines);
        }
    }
}
