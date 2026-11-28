package pro.verron.officestamper.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/// A paragraph in a document, exposed without leaking the underlying OOXML library.
/// Implementations are supplied by a concrete provider (e.g. docx4j 11 or 17).
public interface Paragraph {

    /// Replaces the given contiguous elements with the new elements.
    ///
    /// @param toRemove the elements to remove from the paragraph
    /// @param toAdd    the elements to insert in their place
    void replace(List<Element> toRemove, List<Element> toAdd);

    /// Removes this paragraph from the document.
    void remove();

    /// Replaces a placeholder expression within this paragraph with the given insert.
    ///
    /// @param expression the expression to replace
    /// @param insert     the insert whose elements replace the placeholder
    void replace(String expression, Insert insert);

    /// Replaces the section of elements between `start` and `end` with the given insert.
    ///
    /// @param start  the first element of the section to replace
    /// @param end    the last element of the section to replace
    /// @param insert the insert whose elements replace the section
    void replace(Element start, Element end, Insert insert);

    /// @return the text content of this paragraph
    String asString();

    /// @return the mutable content of this paragraph
    Content content();

    /// Applies the given consumer to the content of this paragraph.
    ///
    /// @param consumer the consumer applied to each content element
    void apply(Consumer<Element> consumer);

    /// @return the comments anchored in this paragraph
    Collection<Comment> getComments();

    /// @return the parent table row of this paragraph, if it is inside a table row
    Optional<Table.Row> parentTableRow();

    /// @return the parent table of this paragraph, if it is inside a table
    Optional<Table> parentTable();
}
