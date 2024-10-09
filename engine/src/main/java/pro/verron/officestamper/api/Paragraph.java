package pro.verron.officestamper.api;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import pro.verron.officestamper.core.StandardComment;

import java.util.Deque;
import java.util.List;

/**
 * The Paragraph interface represents a paragraph in a text document.
 * It provides methods for replacing a placeholder within the paragraph and retrieving the paragraph as a string.
 */
public interface Paragraph {

    void replace(List<P> toRemove, List<P> toAdd);

    void remove();

    StandardComment fakeComment(DocxPart source, Placeholder placeholder);

    R firstRun(); // TODO replace with API not exposing the docx4j API directly

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

    List<Object> paragraphContent(); // TODO replace with API not exposing the docx4j API directly

    Object parent(); // TODO replace with API not exposing the docx4j API directly
}
