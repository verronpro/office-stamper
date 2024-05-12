package pro.verron.officestamper.api;

import org.docx4j.wml.R;

/**
 * The Paragraph interface represents a paragraph in a text document.
 * It provides methods for replacing a placeholder within the paragraph and retrieving the paragraph as a string.
 */
public interface Paragraph {

    /**
     * Replaces all occurrences of a placeholder with a specified replacement value within a paragraph.
     *
     * @param placeholder The placeholder to be replaced.
     * @param replacement The replacement value for the placeholder.
     */
    default void replaceAll(Placeholder placeholder, R replacement) {
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
     */
    default boolean contains(String expression) {
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
}
