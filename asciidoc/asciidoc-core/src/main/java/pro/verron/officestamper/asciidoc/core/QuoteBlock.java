package pro.verron.officestamper.asciidoc.core;

import java.util.List;

/// QuoteBlock.
///
/// @param inlines inline fragments
public record QuoteBlock(List<Inline> inlines)
        implements Block {
    /// Constructor.
    ///
    /// @param inlines inline fragments
    public QuoteBlock(List<Inline> inlines) {
        this.inlines = List.copyOf(inlines);
    }

    @Override
    public int size() {
        return 1;
    }
}
