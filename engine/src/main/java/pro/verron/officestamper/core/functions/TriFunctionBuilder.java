package pro.verron.officestamper.core.functions;

import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.core.DocxStamperConfiguration;
import pro.verron.officestamper.utils.TriFunction;

import java.util.List;
import java.util.function.Function;

/// A builder class for defining and registering custom TriFunction implementations.
/// This class is responsible for constructing a TriFunction implementation
/// with three input types and registering it within a provided `DocxStamperConfiguration`.
/// The TriFunction allows execution of a specific behavior based on the three input arguments
/// and provides a result.
///
/// @param <T> the type of the first input to the TriFunction
/// @param <U> the type of the second input to the TriFunction
/// @param <V> the type of the third input to the TriFunction
public class TriFunctionBuilder<T, U, V>
        implements CustomFunction.NeedsTriFunctionImpl<T, U, V> {
    private final DocxStamperConfiguration source;
    private final String name;
    private final Class<T> class0;
    private final Class<U> class1;
    private final Class<V> class2;

    /// Constructs a new instance of TriFunctionBuilder.
    /// This constructor initializes the TriFunctionBuilder with the given configuration, name, and input types.
    /// It prepares the builder to define and register a custom TriFunction.
    ///
    /// @param source the DocxStamperConfiguration to which the custom TriFunction will be registered
    /// @param name   the name of the custom TriFunction being defined
    /// @param class0 the class of the first input type of the TriFunction
    /// @param class1 the class of the second input type of the TriFunction
    /// @param class2 the class of the third input type of the TriFunction
    public TriFunctionBuilder(
            DocxStamperConfiguration source,
            String name,
            Class<T> class0,
            Class<U> class1,
            Class<V> class2
    ) {
        this.source = source;
        this.name = name;
        this.class0 = class0;
        this.class1 = class1;
        this.class2 = class2;
    }

    /// Registers a custom implementation of a `TriFunction` that operates on three
    /// input arguments of types `T`, `U`, and `V`, and produces a result.
    /// The provided implementation is encapsulated as a `CustomFunction` and added
    /// to the underlying configuration for later use.
    ///
    /// @param implementation the `TriFunction` implementation to register. This function
    ///                       takes three input arguments of types `T`, `U`,
    ///                       and `V` and produces a result.
    @Override
    public void withImplementation(TriFunction<T, U, V, ?> implementation) {
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
