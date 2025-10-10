package pro.verron.officestamper.preset;

import org.docx4j.wml.SectPr;
import pro.verron.officestamper.api.Comment;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Represents a collection of paragraphs in a document, providing various methods to
 * access and process the content within the paragraphs.
 * <p>
 * This record encapsulates metadata and content related to paragraphs, including
 * comment metadata, a data iterator, a list of elements, an optional previous section break,
 * and an indicator for whether there are an odd number of breaks.
 *
 * @param comment              the comment metadata associated with the paragraphs
 * @param data                 an iterator over the data elements in the paragraphs
 * @param elements             a list of objects representing elements in the paragraphs
 * @param previousSectionBreak an optional previous section break associated with the paragraphs
 * @param oddNumberOfBreaks    a flag indicating if there is an odd number of breaks
 */
public record Paragraphs(
        Comment comment,
        Iterator<Object> data,
        List<Object> elements,
        Optional<SectPr> previousSectionBreak,
        boolean oddNumberOfBreaks
) {
    /**
     * Filters and retrieves elements from the internal list that are instances of the specified class type.
     *
     * @param <T>    the type of elements to be filtered and returned
     * @param aClass the class type used to filter and retrieve elements
     * @return a list of elements that are instances of the specified class type
     */
    public <T> List<T> elements(Class<T> aClass) {
        return elements()
                .stream()
                .filter(aClass::isInstance)
                .map(aClass::cast)
                .toList();
    }
}
