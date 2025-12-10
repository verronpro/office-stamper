package pro.verron.officestamper.core.functions;

import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/// A builder class for creating and registering bifunctional implementations with a given configuration.
///
/// This class helps register a [BiFunction] as a [CustomFunction] for the given [OfficeStamperConfiguration]
///
/// @param <T> the type of the first input parameter for the BiFunction
/// @param <U> the type of the second input parameter for the BiFunction
public class BiFunctionBuilder<T, U>
        implements CustomFunction.NeedsBiFunctionImpl<T, U> {
    private final DocxStamperConfiguration source;
    private final String name;
    private final Class<T> class0;
    private final Class<U> class1;

    /// Constructs a new `BiFunctionBuilder` instance, which enables the creation and registration of a bifunctional
    /// implementation with the specified source configuration.
    ///
    /// @param source the configuration instance where the custom function will be registered
    /// @param name the name given to the bifunctional custom function to identify it.
    /// @param class0 the `Class` type that represents the type of the first input parameter.
    /// @param class1 the `Class` type that represents the type of the second input parameter.

    public BiFunctionBuilder(DocxStamperConfiguration source, String name, Class<T> class0, Class<U> class1) {
        this.source = source;
        this.name = name;
        this.class0 = class0;
        this.class1 = class1;
    }

    /// Registers a BiFunction implementation as a custom function within the current context.
    ///
    /// The provided implementation is converted into a generic custom function that accepts a list of arguments and
    /// produces a result. This method enables the addition of a bifunctional logic to the associated configuration,
    /// which can be invoked later with the defined parameter and behavior.
    ///
    /// @param implementation the BiFunction implementation to register, taking two input arguments types `T`
    ///         and `U`, and producing a result.
    @Override
    public OfficeStamperConfiguration withImplementation(BiFunction<T, U, ?> implementation) {
        Function<List<Object>, Object> function = args -> {
            var arg0 = class0.cast(args.getFirst());
            var arg1 = class1.cast(args.get(1));
            return implementation.apply(arg0, arg1);
        };
        var customFunction = new CustomFunction(name, List.of(class0, class1), function);
        source.addCustomFunction(customFunction);
        return source;
    }
}
