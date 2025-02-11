package pro.verron.officestamper.experimental;


import java.util.ArrayList;
import java.util.List;

/// The PowerpointCollector class is used to collect instances of a specific class in a PowerPoint presentation.
///
/// @param <T> the type of instances to collect
public class PowerpointCollector<T>
        extends PowerpointVisitor {
    private final Class<T> aClass;
    private final List<T> list = new ArrayList<>();

    /// The PowerpointCollector class is used to collect instances of a specific class in a PowerPoint presentation.
    ///
    /// @param aClass the type of instances to collect
    public PowerpointCollector(Class<T> aClass) {
        this.aClass = aClass;
    }

    /// Collects instances of a specific class in a PowerPoint presentation.
    ///
    /// @param <T>      the type of instances to collect
    /// @param template the PowerPoint presentation template
    /// @param aClass   the type of instances to collect
    ///
    /// @return a list of instances of the specified class
    public static <T> List<T> collect(
            Object template,
            Class<T> aClass
    ) {
        var collector = new PowerpointCollector<>(aClass);
        collector.visit(template);
        return collector.collect();
    }

    /// Retrieves the collected instances of a specific class.
    ///
    /// @return an instance list of the specified class
    public List<T> collect() {
        return list;
    }

    @Override
    protected void before(Object object) {
        if (aClass.isInstance(object))
            list.add(aClass.cast(object));
    }
}
