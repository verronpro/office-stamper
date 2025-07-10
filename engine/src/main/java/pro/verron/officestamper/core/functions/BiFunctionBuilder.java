package pro.verron.officestamper.core.functions;

import org.jetbrains.annotations.Contract;
import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A builder class for creating and registering bi-functional implementations with a given configuration.
 * This class is responsible for bridging a BiFunction implementation into a custom function that
 * can be utilized within the provided configuration context.
 *
 * @param <T> the type of the first input parameter for the BiFunction
 * @param <U> the type of the second input parameter for the BiFunction
 */
public class BiFunctionBuilder<T, U>
        implements CustomFunction.NeedsBiFunctionImpl<T, U> {
    private final DocxStamperConfiguration source;
    private final String name;
    private final Class<T> class0;
    private final Class<U> class1;

    /**
     * Constructs a new {@code BiFunctionBuilder} instance, which enables the creation and registration
     * of a bi-functional implementation with the specified source configuration.
     *
     * @param source the configuration instance where the custom function will be registered
     * @param name the name given to the bi-functional custom function to identify it
     * @param class0 the {@code Class} type that represents the type of the first input parameter
     * @param class1 the {@code Class} type that represents the type of the second input parameter
     */
    @Contract(pure = true)
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
