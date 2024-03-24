package pro.verron.docxstamper.core;


import java.util.ArrayList;
import java.util.List;

public class PowerpointCollector<T>
        extends PowerpointVisitor {
    private final Class<T> aClass;
    private final List<T> list = new ArrayList<>();

    public PowerpointCollector(Class<T> aClass) {
        this.aClass = aClass;
    }

    public static <T> List<T> collect(
            Object template,
            Class<T> aClass
    ) {
        var collector = new PowerpointCollector<>(aClass);
        collector.visit(template);
        return collector.collect();
    }

    public List<T> collect() {
        return list;
    }

    @Override
    protected void before(Object object) {
        if (aClass.isInstance(object))
            list.add(aClass.cast(object));
    }
}
