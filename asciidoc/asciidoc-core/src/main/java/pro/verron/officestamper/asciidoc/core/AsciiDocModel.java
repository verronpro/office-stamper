package pro.verron.officestamper.asciidoc.core;

import java.util.*;

/// Represents a minimal in-memory model of an AsciiDoc document.
///
/// This model intentionally supports a compact subset sufficient for rendering to WordprocessingML and JavaFX Scene: -
/// Headings (levels 1..6) using leading '=' markers - Paragraphs separated by blank lines - Inline emphasis for bold
/// and italic using AsciiDoc-like markers: *bold*, _italic_
public final class AsciiDocModel {
    private final List<Block> blocks;
    private final Map<String, String> attributes;

    private AsciiDocModel(List<Block> blocks, Map<String, String> attributes) {
        this.blocks = List.copyOf(blocks);
        this.attributes = Map.copyOf(attributes);
    }

    /// Creates a new [AsciiDocModel] from the provided blocks.
    ///
    /// @param blocks ordered content blocks
    ///
    /// @return immutable AsciiDocModel
    public static AsciiDocModel of(List<Block> blocks) {
        return of(blocks, Map.of());
    }

    /// Creates a new [AsciiDocModel] from the provided blocks and attributes.
    ///
    /// @param blocks ordered content blocks
    /// @param attributes document attributes
    ///
    /// @return immutable AsciiDocModel
    public static AsciiDocModel of(List<Block> blocks, Map<String, String> attributes) {
        Objects.requireNonNull(blocks, "blocks");
        Objects.requireNonNull(attributes, "attributes");
        return new AsciiDocModel(new ArrayList<>(blocks), new HashMap<>(attributes));
    }

    /// Returns the ordered list of blocks comprising the document.
    ///
    /// @return immutable list of blocks
    public List<Block> getBlocks() {
        return blocks;
    }

    /// Returns the document attributes.
    ///
    /// @return immutable map of attributes
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
