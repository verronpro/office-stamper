package pro.verron.officestamper.asciidoc;

import java.util.*;

import static java.util.Collections.emptyList;

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

    /// Creates a new [AsciiDocModel] from the provided blocks.
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
            permits Blockquote, Break, CodeBlock, CommentLine, Heading, ImageBlock, MacroBlock, OpenBlock,
            OrderedList, Paragraph, Table, UnorderedList {
        int size();
    }

    /// Inline fragment inside a paragraph/heading.
    public sealed interface Inline
            permits Bold, InlineImage, InlineMacro, Italic, Link, Styled, Sub, Sup, Tab, Text {
        /// Returns the text of the inline fragment.
        ///
        /// @return text
        String text();
    }

    /// Heading block (levels 1..6).
    ///
    /// @param level heading level
    /// @param inlines inline fragments
    public record Heading(List<String> header, int level, List<Inline> inlines)
            implements Block {
        /// Constructs a Heading object with the specified heading level and inline fragments.
        ///
        /// @param level the heading level, must be between 1 and 6 (inclusive)
        /// @param inlines the list of inline fragments representing the content of the heading
        /// @throws IllegalArgumentException if the heading level is outside the range of 1 to 6
        public Heading(int level, List<Inline> inlines) {
            this(emptyList(), level, inlines);
        }

        /// Constructor.
        ///
        /// @param level heading level
        /// @param inlines inline fragments
        public Heading(List<String> header, int level, List<Inline> inlines) {
            if (level < 1 || level > 6) {
                throw new IllegalArgumentException("Heading level must be between 1 and 6");
            }
            this.header = List.copyOf(header);
            this.level = level;
            this.inlines = List.copyOf(inlines);
        }

        @Override
        public int size() {
            return 1;
        }
    }

    /// Paragraph block.
    ///
    /// @param inlines inline fragments
    public record Paragraph(List<String> header, List<Inline> inlines)
            implements Block {
        /// Constructs a Paragraph object with the specified list of inline elements.
        /// This constructor initializes the paragraph without any header and sets the
        /// inline fragments to the provided list.
        ///
        /// @param inlines the list of inline elements that make up the paragraph
        public Paragraph(List<Inline> inlines) {
            this(emptyList(), inlines);
        }

        /// Constructor.
        ///
        /// @param inlines inline fragments
        public Paragraph(List<String> header, List<Inline> inlines) {
            this.header = List.copyOf(header);
            this.inlines = List.copyOf(inlines);
        }

        @Override
        public int size() {
            return 1;
        }
    }

    /// Text fragment.
    ///
    /// @param text text
    public record Text(String text)
            implements Inline {}

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
            StringBuilder sb = new StringBuilder();
            for (Inline in : children) sb.append(in.text());
            return sb.toString();
        }
    }

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

    /// Table row.
    ///
    /// @param cells table cells
    public record Row(List<Cell> cells, Optional<String> style) {
        /// Constructor.
        ///
        /// @param cells table cells
        public Row(List<Cell> cells) {
            this(cells, Optional.empty());
        }

        public Row(List<Cell> cells, Optional<String> style) {
            this.cells = List.copyOf(cells);
            this.style = style;
        }

        /// Creates a list containing a single [Row] instance, where the row itself contains a list of default [Cell]
        ///  instances.
        ///
        /// @return an immutable list with one [Row], which wraps a list of predefined [Cell] objects
        public static List<Row> listOf() {
            return List.of(of(Cell.listOf()));
        }

        private static Row of(List<Cell> cells) {
            return new Row(cells);
        }
    }

    /// Table cell.
    ///
    /// @param blocks cell content blocks
    public record Cell(List<Block> blocks, Optional<String> style) {
        public Cell(List<Block> blocks) {
            this(blocks, Optional.empty());
        }

        /// Constructor.
        ///
        /// @param blocks cell content blocks
        public Cell(List<Block> blocks, Optional<String> style) {
            this.blocks = List.copyOf(blocks);
            this.style = style;
        }

        private static List<Cell> listOf() {
            return List.of(ofInlines(List.of(new Text("A"))), ofInlines(List.of(new Text("B"))));
        }

        /// Creates a new [Cell] instance by wrapping a list of [Inline] elements
        /// into a [Paragraph] and adding it to the cell's content blocks.
        ///
        /// @param inlines the list of [Inline] elements to be wrapped into a [Paragraph]
        /// @return a [Cell] containing the specified [Inline] elements as a single [Paragraph]
        public static Cell ofInlines(List<Inline> inlines) {
            return new Cell(List.of(new Paragraph(inlines)));
        }
    }

    /// Unordered list.
    ///
    /// @param items list items
    public record UnorderedList(List<ListItem> items)
            implements Block {
        @Override
        public int size() {
            return items.size();
        }
    }

    /// Ordered list.
    ///
    /// @param items list items
    public record OrderedList(List<ListItem> items)
            implements Block {
        @Override
        public int size() {
            return items.size();
        }
    }

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

    /// Blockquote.
    ///
    /// @param inlines inline fragments
    public record Blockquote(List<Inline> inlines)
            implements Block {
        /// Constructor.
        ///
        /// @param inlines inline fragments
        public Blockquote(List<Inline> inlines) {
            this.inlines = List.copyOf(inlines);
        }

        @Override
        public int size() {
            return 1;
        }
    }

    /// Code block.
    ///
    /// @param language language
    /// @param content code content
    public record CodeBlock(String language, String content)
            implements Block {
        @Override
        public int size() {
            return 1;
        }
    }

    /// Image block.
    ///
    /// @param url image URL
    /// @param altText alternative text
    public record ImageBlock(String url, String altText)
            implements Block {
        @Override
        public int size() {
            return 1;
        }
    }

    /// Link inline.
    ///
    /// @param url link URL
    /// @param text link text
    public record Link(String url, String text)
            implements Inline {}

    /// Inline image.
    ///
    /// @param path image path
    /// @param map alternative text
    public record InlineImage(String path, Map<String, String> map)
            implements Inline {

        /// Constructs an instance of the InlineImage class with the specified image path and alternative text mappings.
        ///
        /// @param path the path to the image
        /// @param map a mapping of alternative text attributes associated with the image;
        ///            keys and values represent descriptive labels for different use cases or locales
        public InlineImage(String path, Map<String, String> map) {
            this.path = path;
            this.map = Collections.unmodifiableMap(new TreeMap<>(map));
        }

        @Override
        public String text() {
            return path;
        }
    }


    /// Represents an OpenBlock element in an AsciiDoc model.
    ///
    /// An OpenBlock is a container for grouping other blocks and includes both header and content sections.
    /// The header contains metadata or data relevant to the block, while the content is the list of
    /// individual blocks encapsulated within this OpenBlock.
    ///
    /// This implementation computes the size of the OpenBlock as the sum of the sizes of its content blocks.
    ///
    /// @param header  a list of strings representing metadata or informational content about the OpenBlock.
    /// @param content a list of [Block] elements comprising the actual blocks grouped by this OpenBlock.
    ///
    /// @see Block
    public record OpenBlock(List<String> header, List<Block> content)
            implements Block {
        @Override
        public int size() {
            return content.stream()
                          .mapToInt(Block::size)
                          .sum();
        }
    }

    /// Represents a line break in a document structure.
    /// This is a marker record that implements the [Block] interface.
    /// It has a fixed size of zero, indicating no content spans across the line break.
    public record Break()
            implements Block {
        @Override
        public int size() {
            return 0;
        }
    }

    /// Represents a comment line in the AsciiDoc document model.
    ///
    /// A comment line is considered a block-level element but does not contribute
    /// any visible content to the output document. It is typically used to store
    /// annotations or additional information within the block structure.
    ///
    /// This class implements the [Block] interface, which mandates
    /// implementing the [#size()] method. The size of a comment line
    /// is always zero, as it does not represent any visual or measurable content.
    ///
    /// @param comment the text of the comment line
    public record CommentLine(String comment)
            implements Block {
        @Override
        public int size() {
            return 0;
        }
    }

    /// Represents an inline fragment within a paragraph or heading that is styled with a specific role.
    ///
    /// A Styled instance encapsulates:
    /// - A role, which defines the style or semantic meaning associated with the content.
    /// - A list of children, which are other inline elements that are part of this styled fragment.
    ///
    /// This record implements the Inline interface, providing functionality to retrieve
    /// styled text by concatenating the text from all its child inline elements.
    ///
    /// Responsibilities:
    /// - Holds a role and its associated inline content.
    /// - Provides a textual representation of the styled content by aggregating the text
    ///   from all children inline elements.
    public record Styled(String role, List<Inline> children)
            implements Inline {
        @Override
        public String text() {
            StringBuilder sb = new StringBuilder();
            for (Inline in : children) sb.append(in.text());
            return sb.toString();
        }
    }

    /// Represents a macro block in an AsciiDoc document, which is a specialized block
    /// containing a unique identifier, a name, and a list of associated data.
    ///
    /// The [MacroBlock] is immutable and implements the [Block] interface.
    /// It provides a concrete implementation for determining the size of the block.
    ///
    /// @param name the name of the macro block
    /// @param id the unique identifier for the macro block
    /// @param list an ordered list of strings associated with the macro block
    public record MacroBlock(String name, String id, List<String> list)
            implements Block {
        @Override
        public int size() {
            return 1;
        }
    }

    /// Represents an inline macro in an AsciiDoc document.
    /// An inline macro is a specialized inline element with a name, an identifier,
    /// and a list of string values that represent its content.
    ///
    /// @param name the name of the macro, describing its purpose or type
    /// @param id   an identifier associated with the macro, often used for reference
    /// @param list a list of strings representing the components of the macro's content
    public record InlineMacro(String name, String id, List<String> list)
            implements Inline {

        @Override
        public String text() {
            return String.join("", list);
        }
    }
}
