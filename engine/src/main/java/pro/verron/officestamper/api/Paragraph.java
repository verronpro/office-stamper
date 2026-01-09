package pro.verron.officestamper.api;

import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/// The Paragraph interface represents a paragraph in a text document.
/// It provides methods for replacing a placeholder within the paragraph and retrieving the paragraph as a string.
public interface Paragraph {

    /// Replaces specified contiguous elements within the paragraph with new elements.
    ///
    /// @param toRemove The list of elements to be removed from the paragraph.
    /// @param toAdd The list of elements to be added to the paragraph.
    void replace(List<P> toRemove, List<P> toAdd);

    /// Removes the paragraph from the document.
    /// This method is intended to be used when a paragraph needs to be deleted.
    void remove();

    /// Replaces a specified placeholder within the paragraph with the provided insert.
    ///
    /// @param expression the expression to replace.
    /// @param insert     the insert containing elements that will replace the placeholder.
    void replace(String expression, Insert insert);

    /// Replaces a section of elements within the document, defined by the start and end objects,
    /// with the elements provided by the given insert.
    ///
    /// @param start  the starting object marking the beginning of the section to replace.
    /// @param end    the ending object marking the end of the section to replace.
    /// @param insert the insert containing the elements that will replace the specified section.
    void replace(Object start, Object end, Insert insert);

    ///Returns the paragraph as a string.
    ///
    ///@return the paragraph as a string
    String asString();

    /// Applies the specified consumer function to the paragraph content.
    ///
    /// @param pConsumer The consumer function to apply to the paragraph content.
    void apply(Consumer<ContentAccessor> pConsumer);

    /// Retrieves the parent of the current paragraph that matches the specified class type.
    ///
    /// @param aClass The class type to match for the parent element.
    /// @param <T> The type of the parent element to be returned.
    /// @return An [Optional] containing the matched parent element if found, otherwise an empty [Optional].
    <T> Optional<T> parent(Class<T> aClass);

    /// Retrieves a collection of comments associated with the paragraph.
    ///
    /// @return a collection of [Comments.Comment] objects related to the paragraph
    Collection<Comments.Comment> getComment();


    /// Retrieves the parent table row of the current paragraph, if it exists.
    ///
    /// @return an [Optional] containing the parent [Table.Row] if the paragraph is within a table row, otherwise an
    ///         empty [Optional]
    Optional<Table.Row> parentTableRow();

    /// Retrieves the parent table of the current paragraph, if it exists.
    ///
    /// @return an [Optional] containing the parent [Table] if the paragraph is within a table, otherwise an empty
    ///         [Optional]
    Optional<Table> parentTable();
}
