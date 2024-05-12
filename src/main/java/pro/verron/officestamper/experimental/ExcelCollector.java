package pro.verron.officestamper.experimental;

import java.util.ArrayList;
import java.util.List;

/**
 * The ExcelCollector class is used to collect objects of a specific type from an Excel file.
 * It extends the ExcelVisitor class and overrides the 'before' method to add the objects of the given type to a list.
 *
 * @param <T> the type of objects to collect
 */
public class ExcelCollector<T>
        extends ExcelVisitor {
    private final Class<T> aClass;
    private final List<T> list = new ArrayList<>();

    public ExcelCollector(Class<T> aClass) {
        this.aClass = aClass;
    /**
     * Constructs a new ExcelCollector object with the given type.
     *
     * @param type the class representing the type of objects to collect
     */
    }

    /**
     * Collects objects of a specific type from an Excel file.
     *
     * @param <T>    the type of objects to collect
     * @param object the Excel file or object to collect from
     * @param type   the class representing the type of objects to collect
     *
     * @return a List containing the collected objects
     */
    public static <T> List<T> collect(
            Object template,
            Class<T> aClass
    ) {
        var collector = new ExcelCollector<>(aClass);
        collector.visit(template);
        return collector.collect();
    }

    /**
     * Returns a List containing the collected objects.
     *
     * @return a List containing the collected objects
     */
    public List<T> collect() {
        return list;
    }

    /**
     * This method is called before visiting an object in the ExcelCollector class.
     * It checks if the object is an instance of the specified type and adds it to a list if it is.
     *
     * @param object the object being visited
     */
    @Override
    protected void before(Object object) {
        if (aClass.isInstance(object))
            list.add(aClass.cast(object));
    }
}
