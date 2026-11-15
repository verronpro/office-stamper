package pro.verron.officestamper.spi;

import java.util.List;

/// A container of elements to be inserted into a document.
///
/// @param elements the immutable list of elements to insert
public record Insert(List<Element> elements) {

    /// Creates an insert holding a single element.
    ///
    /// @param element the element to insert
    public Insert(Element element) {
        this(List.of(element));
    }

    /// Normalizes the element list into an immutable copy.
    public Insert {
        elements = List.copyOf(elements);
    }
}
