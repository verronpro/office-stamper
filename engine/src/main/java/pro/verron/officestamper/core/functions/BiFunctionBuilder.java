package pro.verron.officestamper.core.functions;

import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BiFunctionBuilder<T, U>
        implements CustomFunction.NeedsBiFunctionImpl<T, U> {
    private final DocxStamperConfiguration source;
    private final String name;
    private final Class<T> class0;
    private final Class<U> class1;

    public BiFunctionBuilder(DocxStamperConfiguration source, String name, Class<T> class0, Class<U> class1) {
        this.source = source;
        this.name = name;
        this.class0 = class0;
        this.class1 = class1;
    }

    @Override public void withImplementation(BiFunction<T, U, ?> implementation) {
        Function<List<Object>, Object> function = args -> {
            var arg0 = class0.cast(args.getFirst());
            var arg1 = class1.cast(args.get(1));
            return implementation.apply(arg0, arg1);
        };
        var customFunction = new CustomFunction(name, List.of(class0, class1), function);
        source.addCustomFunction(customFunction);
    }
}
