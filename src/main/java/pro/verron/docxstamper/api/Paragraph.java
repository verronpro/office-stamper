package pro.verron.docxstamper.api;

/**
 * The Paragraph interface represents a paragraph in a text document.
 * It provides methods for replacing a placeholder within the paragraph and retrieving the paragraph as a string.
 */
public interface Paragraph {

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
