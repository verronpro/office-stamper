package pro.verron.docxstamper.test.utils;

import org.docx4j.TraversalUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>DocxCollector class.</p>
 *
 * @since 1.6.5
 * @author Joseph Verron
 * @version ${version}
 */
public class DocxCollector<T> extends TraversalUtil.CallbackImpl {

    private final Set<T> elements = new LinkedHashSet<>();
    private final Class<T> type;

    /**
     * <p>Constructor for DocxCollector.</p>
     *
     * @param type a {@link java.lang.Class} object
     * @since 1.6.6
     */
    public DocxCollector(Class<T> type) {
        super();
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    public List<Object> apply(Object o) {
        if (type.isInstance(o)) {
            elements.add(type.cast(o));
        }
        return List.of(elements);
    }

    /**
     * <p>elements.</p>
     *
     * @return a {@link java.util.stream.Stream} object
     * @since 1.6.6
     */
    public Stream<T> elements() {
        return elements.stream();
    }
}
