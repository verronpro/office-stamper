package pro.verron.officestamper.core.functions;

import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import java.util.List;
import java.util.function.Function;

public class FunctionBuilder<T>
        implements CustomFunction.NeedsFunctionImpl<T> {
    private final DocxStamperConfiguration source;
    private final String name;
    private final Class<T> class0;

    public FunctionBuilder(DocxStamperConfiguration source, String name, Class<T> class0) {
        this.source = source;
        this.name = name;
        this.class0 = class0;
    }

    @Override public void withImplementation(Function<T, ?> implementation) {
        Function<List<Object>, Object> objectFunction = args -> {
            var arg0 = class0.cast(args.getFirst());
            return implementation.apply(arg0);
        };
        var customFunction = new CustomFunction(name, List.of(class0), objectFunction);
        source.addCustomFunction(customFunction);
    }

}
