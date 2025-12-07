package pro.verron.officestamper.api;

import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static java.util.Collections.singletonList;

/// The Insert record represents a container for managing collections of document elements that can be inserted into a
/// DOCX document.
///
/// This record is used to wrap various types of document elements such as text runs, smart tag runs, and other WML
/// objects that can be inserted into a document.
public record Insert(List<Object> elements) {

    public Insert(Object element) {
        this(singletonList(element));
    }

    public Insert {
        elements = List.copyOf(elements);
    }

    public void setRPr(@Nullable RPr rPr) {
        elements.stream()
                .filter(R.class::isInstance)
                .map(R.class::cast)
                .forEach(r -> r.setRPr(rPr));
    }

    public <T> T assertSingleton(Class<T> clazz) {
        if (elements.size() != 1) throw new AssertionError("Insert must contain exactly one element");
        var element = elements.getFirst();
        if (clazz.isInstance(element)) return clazz.cast(element);
        throw new AssertionError("Insert '%s' is not a unique element of expected type '%s'".formatted(element, clazz));
    }
}
