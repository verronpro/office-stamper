package pro.verron.officestamper.spi;

import java.util.List;

/// A container of elements to be inserted into a document.
///
/// <p>The insert may carry [RunProperties] ({@code null} by default); the provider applies them to the
/// inserted runs, which is how the formatting of a replaced run is preserved.
///
/// @param elements      the immutable list of elements to insert
/// @param runProperties the formatting to apply to the inserted runs, or {@code null} for none
public record Insert(List<Element> elements, RunProperties runProperties) {

    /// Creates an insert holding a single element, with no run properties.
    ///
    /// @param element the element to insert
    public Insert(Element element) {
        this(List.of(element), null);
    }

    /// Creates an insert holding the given elements, with no run properties.
    ///
    /// @param elements the elements to insert
    public Insert(List<Element> elements) {
        this(elements, null);
    }

    /// Normalizes the element list into an immutable copy.
    public Insert {
        elements = List.copyOf(elements);
    }

    /// Returns a copy of this insert carrying the given run properties, to be applied by the provider
    /// to every inserted run.
    ///
    /// @param runProperties the formatting to apply, or {@code null} for none
    /// @return a new insert with the given run properties
    public Insert withRunProperties(RunProperties runProperties) {
        return new Insert(elements, runProperties);
    }
}
