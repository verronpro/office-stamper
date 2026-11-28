package pro.verron.officestamper.api;

import java.util.Optional;

/// A [ProcessorContext] that carries the paragraph under evaluation.
///
/// Used by hooks anchored on a paragraph without a comment (placeholder and inline
/// tags). The paragraph is always present, so [tableRow] and [table]
/// simply delegate to it.
public class ParagraphProcessorContext extends ProcessorContext {

    private final Paragraph paragraph;

    /// Constructs a processor context.
    ///
    /// @param part        the part being processed
    /// @param paragraph   the paragraph being processed
    /// @param expression  the expression being evaluated
    /// @param contextTree the context tree managing scopes
    public ParagraphProcessorContext(
            OfficePart part,
            Paragraph paragraph,
            String expression,
            ContextTree contextTree
    ) {
        super(part, expression, contextTree);
        this.paragraph = paragraph;
    }

    /// @return the paragraph being processed
    public Paragraph paragraph() {
        return paragraph;
    }

    /// @return the table row containing the current paragraph, if any
    public Optional<Table.Row> tableRow() {
        return paragraph.parentTableRow();
    }

    /// @return the table containing the current paragraph, if any
    public Optional<Table> table() {
        return paragraph.parentTable();
    }
}
