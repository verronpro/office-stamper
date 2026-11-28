package pro.verron.officestamper.api;

import java.util.Iterator;

/// A [ParagraphProcessorContext] that carries the anchored comment driving the operation.
///
/// Used by comment-anchored hooks. The comment is always present, so
/// [comment] returns it directly and [contentIterator] delegates to
/// the comment's range slice.
public class CommentProcessorContext extends ParagraphProcessorContext {

    private final AnchoredComment comment;

    /// Constructs a processor context.
    ///
    /// @param part        the part being processed
    /// @param paragraph   the paragraph being processed
    /// @param comment     the anchored comment relevant to this context
    /// @param expression  the expression being evaluated
    /// @param contextTree the context tree managing scopes
    public CommentProcessorContext(
            OfficePart part,
            Paragraph paragraph,
            AnchoredComment comment,
            String expression,
            ContextTree contextTree
    ) {
        super(part, paragraph, expression, contextTree);
        this.comment = comment;
    }

    /// @return the anchored comment associated with this context
    public AnchoredComment comment() {
        return comment;
    }

    /// @return an iterator over the content between the anchored comment's range markers (the
    ///         fine-grained, provider-native slice — runs between the anchors, anchors excluded)
    public Iterator<Element> contentIterator() {
        return comment.contentIterator();
    }
}
