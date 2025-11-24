package pro.verron.officestamper.api;

import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/// The Paragraph interface represents a paragraph in a text document.
/// It provides methods for replacing a placeholder within the paragraph and retrieving the paragraph as a string.
public interface Paragraph {
    /// Creates a processor context for the given placeholder within this paragraph.
    ///
    /// @param placeholder The placeholder to create a context for.
    /// @return The processor context for the specified placeholder.
    ProcessorContext processorContext(Placeholder placeholder);

    /// Replaces specified contiguous elements within the paragraph with new elements.
    ///
    /// @param toRemove The list of elements to be removed from the paragraph.
    /// @param toAdd The list of elements to be added to the paragraph.
    void replace(List<P> toRemove, List<P> toAdd);

    /// Removes the paragraph from the document.
    /// This method is intended to be used when a paragraph needs to be deleted.
    void remove();

    /// Replaces a placeholder in the given paragraph with the specified replacement.
    ///
    /// @param placeholder The placeholder to be replaced.
    /// @param replacement The replacement for the placeholder.
    void replace(Placeholder placeholder, Insert insert);

    /// Replaces a slice of objects in the given paragraph with the specified replacement.
    ///
    /// @param from The first object to be replaced.
    /// @param to The last object for the placeholder.
    /// @param replacement The replacement for the placeholder.
    void replace(Object from, Object to, Insert insert);

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
    /// @return An `Optional` containing the matched parent element if found, otherwise an empty `Optional`.
    <T> Optional<T> parent(Class<T> aClass);

    /// Retrieves a collection of comments associated with the paragraph.
    ///
    /// @return a collection of `Comments.Comment` objects related to the paragraph
    Collection<Comments.Comment> getComment();
}
