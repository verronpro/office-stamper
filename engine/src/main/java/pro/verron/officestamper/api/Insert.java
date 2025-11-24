package pro.verron.officestamper.api;

import org.docx4j.wml.RPr;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.utils.WmlUtils;

import java.util.SequencedCollection;

/// The Insert interface represents a contract for managing the insertion of objects
/// with specific constraints, such as ensuring serializability and type consistency.
/// It provides methods to access and manipulate collections of elements, validate the
/// type of elements, and set specific properties for the elements when applicable.
public interface Insert {
    default void assertSerializable() {
        for (Object element : getElements()) {
            if (!WmlUtils.serializable(element))
                throw new AssertionError("The inserted objects must be valid DOCX4J Object");
        }
    }

    /// Retrieves a sequenced collection of elements managed by the implementing class.
    ///
    /// @return a SequencedCollection containing the elements
    SequencedCollection<Object> getElements();

    default void setRPr(@Nullable RPr rPr) {/* DO NOTHING*/}

    default <T> T assertInstanceOf(Class<T> clazz) {
        Object first = getElements().getFirst();
        if (clazz.isInstance(first)) return clazz.cast(first);
        throw new AssertionError("Insert '%s' is not a unique element of expected type '%s'".formatted(first, clazz));
    }
}
