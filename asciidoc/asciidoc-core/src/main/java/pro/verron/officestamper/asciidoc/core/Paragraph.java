package pro.verron.officestamper.asciidoc.core;

import java.util.List;

import static java.util.Collections.emptyList;

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
