package pro.verron.officestamper.api;

import java.util.Iterator;
import java.util.List;

/// A comment anchored in a paragraph, exposing its range for document manipulation.
///
/// This is the manipulation-facing comment used by comment processors; the read-only view lives in
/// [Comment] (as returned by [OfficePart#comments()]).
///
/// **Migration path** from the pre-2.0 docx4j-coupled `Comment`: the methods that
/// exposed docx4j types directly (`getStartTagRun`, `getCommentRangeStart`, `getCommentRangeEnd`,
/// `getCommentReference`, `getComment`, `getParent`) are replaced by two things:
///
/// - the docx4j-free accessors [startAnchor], [endAnchor] and [elements], which feed the
/// docx4j-free flow — e.g. `paragraph().replace(comment.startAnchor(), comment.endAnchor(), insert)`
/// replaces the comment's range, and
/// - [unwrap], which hands back the provider's native representation (e.g. the docx4j
/// `CommentRangeStart`, `CommentRangeEnd`, `Comments.Comment` or `CommentReference` objects when
/// using the docx4j provider) for comment-specific actions that need raw access.
public interface AnchoredComment {

    /// @return the comment identifier
    String id();

    /// @return the author of the comment
    String author();

    /// @return the textual content of the comment
    String text();

    /// @return the expression carried by this comment
    String expression();

    /// @return the paragraph this comment is anchored in
    Paragraph paragraph();

    /// @return the element marking the start of this comment's range
    Element startAnchor();

    /// @return the element marking the end of this comment's range
    Element endAnchor();

    /// @return the elements between this comment's range anchors
    List<Element> elements();

    /// @return a fine-grained iterator over the provider-native content strictly between this
    ///         comment's range anchors (the words slice: nested runs between the markers, markers
    ///         excluded) — the removal granularity of the words-level display conditions
    Iterator<Element> contentIterator();

    /// Returns the native representation of this comment, for library users that implement
    /// comment-specific actions against the provider's format types.
    ///
    /// @param type the expected native type (e.g. the format's range-start, range-end or comment object)
    /// @param <T>  the native type
    /// @return the native representation, cast to the given type
    /// @throws IllegalArgumentException if the provider does not support the requested type
    <T> T unwrap(Class<T> type);

    /// Removes this comment's markers (range start, range end and reference) from the document,
    /// including any hook tag left empty. The commented content itself is kept.
    void delete();

    /// Removes every marker of this comment found within the given element's subtree — used when a
    /// deep copy of commented content (e.g. a table row) is re-inserted elsewhere and must not carry
    /// stale markers.
    ///
    /// @param element the subtree to clean
    void deleteFrom(Element element);
}
