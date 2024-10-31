package pro.verron.officestamper.core.functions;

import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.core.DocxStamperConfiguration;
import pro.verron.officestamper.utils.TriFunction;

import java.util.List;
import java.util.function.Function;

public class TriFunctionBuilder<T, U, V>
        implements CustomFunction.NeedsTriFunctionImpl<T, U, V> {
    private final DocxStamperConfiguration source;
    private final String name;
    private final Class<T> class0;
    private final Class<U> class1;
    private final Class<V> class2;

    public TriFunctionBuilder(
            DocxStamperConfiguration source, String name, Class<T> class0, Class<U> class1, Class<V> class2
    ) {
        this.source = source;
        this.name = name;
        this.class0 = class0;
        this.class1 = class1;
        this.class2 = class2;
    }

    @Override public void withImplementation(TriFunction<T, U, V, ?> implementation) {
        Function<List<Object>, Object> function = args -> {
            var arg0 = class0.cast(args.getFirst());
            var arg1 = class1.cast(args.get(1));
            var arg2 = class2.cast(args.get(2));
            return implementation.apply(arg0, arg1, arg2);
        };
        var customFunction = new CustomFunction(name, List.of(class0, class1, class2), function);
        source.addCustomFunction(customFunction);
    }
}
