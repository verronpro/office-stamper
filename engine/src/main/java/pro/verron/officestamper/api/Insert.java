package pro.verron.officestamper.api;

import org.docx4j.wml.RPr;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.utils.WmlUtils;

import java.util.SequencedCollection;

public interface Insert {
    default void assertSerializable() {
        for (Object element : getElements()) {
            if (!WmlUtils.serializable(element))
                throw new AssertionError("The inserted objects must be valid DOCX4J Object");
        }
    }

    SequencedCollection<?> getElements();

    void setRPr(@Nullable RPr rPr);

    default <T> T assertInstanceOf(Class<T> clazz) {
        Object first = getElements().getFirst();
        if (clazz.isInstance(first))
            return clazz.cast(first);
        throw new AssertionError("Insert '%s' is not a unique element of expected type '%s'".formatted(first, clazz));
    }
}
