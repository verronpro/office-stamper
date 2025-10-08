package pro.verron.officestamper.core.functions;

import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import java.util.List;
import java.util.function.Function;

/// A builder class for creating and registering custom functions in the context of a `DocxStamperConfiguration`.
/// The custom functions are defined by a name, a single input parameter type, and their implementation.
///
/// @param <T> the type of the input to the function
public class FunctionBuilder<T>
        implements CustomFunction.NeedsFunctionImpl<T> {
    private final DocxStamperConfiguration source;
    private final String name;
    private final Class<T> class0;

    /// Constructs a new `FunctionBuilder` to define and register a custom function in the provided
    /// `DocxStamperConfiguration`.
    ///
    /// @param source the `DocxStamperConfiguration` instance in which the custom function will be registered
    /// @param name   the name of the custom function to be defined
    /// @param class0 the `Class` object representing the type of the single input parameter for the custom function
    public FunctionBuilder(DocxStamperConfiguration source, String name, Class<T> class0) {
        this.source = source;
        this.name = name;
        this.class0 = class0;
    }

    /// Sets the implementation for the custom function being built.
    /// The implementation defines the behavior of the function for a specific input type and is wrapped in a
    /// `CustomFunction` instance that is added to the configuration.
    ///
    /// @param implementation a `Function` that takes an input of type `T` and produces a result
    @Override
    public void withImplementation(Function<T, ?> implementation) {
        Function<List<Object>, Object> objectFunction = args -> {
            var arg0 = class0.cast(args.getFirst());
            return implementation.apply(arg0);
        };
        var customFunction = new CustomFunction(name, List.of(class0), objectFunction);
        source.addCustomFunction(customFunction);
    }
}
