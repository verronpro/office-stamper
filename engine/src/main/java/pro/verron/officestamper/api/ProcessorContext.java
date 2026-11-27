package pro.verron.officestamper.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/// Context for a document-processing operation, exposed without leaking the underlying OOXML library.
///
/// It bundles the part being processed, the current paragraph, an optional anchored comment, the
/// expression under evaluation, and the context tree that manages hierarchical scopes.
public final class ProcessorContext {

    private final OfficePart part;
    private final Paragraph paragraph;
    private final AnchoredComment comment;
    private final String expression;
    private final ContextTree contextTree;

    /// Constructs a processor context.
    ///
    /// @param part        the part being processed
    /// @param paragraph   the paragraph being processed
    /// @param comment     the anchored comment relevant to this context, or `null`
    /// @param expression  the expression being evaluated
    /// @param contextTree the context tree managing scopes
    public ProcessorContext(
            OfficePart part,
            Paragraph paragraph,
            AnchoredComment comment,
            String expression,
            ContextTree contextTree
    ) {
        this.part = part;
        this.paragraph = paragraph;
        this.comment = comment;
        this.expression = expression;
        this.contextTree = contextTree;
    }

    /// @return an iterator over the content between the anchored comment's range markers (the
    ///         fine-grained, provider-native slice — runs between the anchors, anchors excluded), or
    ///         an empty iterator when no comment is associated with this context
    public Iterator<Element> contentIterator() {
        return comment == null
                ? Collections.<Element>emptyIterator()
                : comment.contentIterator();
    }

    /// @return the anchored comment associated with this context, or `null`
    public AnchoredComment comment() {
        return comment;
    }

    /// @return the table row containing the current paragraph, if any
    public Optional<Table.Row> tableRow() {
        return paragraph == null ? Optional.empty() : paragraph.parentTableRow();
    }

    /// @return the table containing the current paragraph, if any
    public Optional<Table> table() {
        return paragraph == null ? Optional.empty() : paragraph.parentTable();
    }

    /// @return the part being processed
    public OfficePart part() {
        return part;
    }

    /// @return the paragraph being processed
    public Paragraph paragraph() {
        return paragraph;
    }

    /// @return the expression being evaluated
    public String expression() {
        return expression;
    }

    /// @return the context tree managing scopes for this context
    public ContextTree contextHolder() {
        return contextTree;
    }
}
