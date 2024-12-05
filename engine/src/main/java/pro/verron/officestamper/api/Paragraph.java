package pro.verron.officestamper.api;

import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The Paragraph interface represents a paragraph in a text document.
 * It provides methods for replacing a placeholder within the paragraph and retrieving the paragraph as a string.
 */
public interface Paragraph {

    /**
     * Creates a processor context for the given placeholder within this paragraph.
     *
     * @param placeholder The placeholder to create a context for.
     * @return The processor context for the specified placeholder.
     */
    ProcessorContext processorContext(Placeholder placeholder);

    /**
     * Replaces specified contiguous elements within the paragraph with new elements.
     *
     * @param toRemove The list of elements to be removed from the paragraph.
     * @param toAdd The list of elements to be added to the paragraph.
     */
    void replace(List<P> toRemove, List<P> toAdd);

    /**
     * Removes the paragraph from the document.
     * This method is intended to be used when a paragraph needs to be deleted.
     */
    void remove();

    /**
     * Retrieves the paragraph associated with this object.
     * TODO replace with API not exposing the docx4j API directly
     *
     * @return the paragraph object
     *
     * @deprecated As of version 2.6, due to its direct exposure of the docx4j API. It is scheduled for removal in
     * the future.
     */
    @Deprecated(since = "2.6", forRemoval = true)
    P getP(); // TODO replace with API not exposing the docx4j API directly

    /**
     * Replaces all occurrences of a placeholder with a specified replacement value within a paragraph.
     *
     * @param placeholder The placeholder to be replaced.
     * @param replacement The replacement value for the placeholder.
     *
     * @deprecated was used by the core to deal with multiline paragraphs, users should fallback to
     * {@link #replace(Placeholder, Object)} only
     */
    @Deprecated(since = "2.4", forRemoval = true) default void replaceAll(Placeholder placeholder, R replacement) {
        while (contains(placeholder.expression())) {
            replace(placeholder, replacement);
        }
    }

    /**
     * Returns true if the given expression is found within the paragraph, otherwise returns false.
     *
     * @param expression The string to search for within the paragraph.
     *
     * @return true if the given expression is found within the paragraph, otherwise false.
     *
     * @deprecated was used by the core to deal with multiline paragraphs
     */
    @Deprecated(since = "2.4", forRemoval = true) default boolean contains(String expression) {
        return asString().contains(expression);
    }

    /**
     * Replaces a placeholder in the given paragraph with the specified replacement.
     *
     * @param placeholder The placeholder to be replaced.
     * @param replacement The replacement for the placeholder.
     */
    void replace(Placeholder placeholder, Object replacement);

    /**
     * Returns the paragraph as a string.
     *
     * @return the paragraph as a string
     */
    String asString();

    /**
     * Applies the specified consumer function to the paragraph content.
     *
     * @param pConsumer The consumer function to apply to the paragraph content.
     */
    void apply(Consumer<P> pConsumer);

    /**
     * Retrieves the parent of the current paragraph that matches the specified class type.
     *
     * @param aClass The class type to match for the parent element.
     * @param <T> The type of the parent element to be returned.
     * @return An {@code Optional} containing the matched parent element if found, otherwise an empty {@code Optional}.
     */
    <T> Optional<T> parent(Class<T> aClass);

    Collection<Comments.Comment> getComment();
}
