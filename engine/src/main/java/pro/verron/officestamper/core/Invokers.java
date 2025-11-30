package pro.verron.officestamper.core;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.TypedValue;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.CustomFunction;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/// The Invokers class serves as an implementation of the MethodResolver interface.
///
/// It is designed to provide an efficient mechanism for resolving method executors based on method names and argument
/// types.
///
/// The class organizes and stores registered invokers in a structured map, enabling streamlined method resolution at
/// runtime.
public class Invokers
        implements MethodResolver {
    private final Map<String, Map<Args, MethodExecutor>> map;

    public Invokers(Collection<CustomFunction> functions) {
        var invokerStream = functions.stream()
                                     .map(Invokers::ofCustomFunction);
        this(invokerStream);
    }

    private static Invoker ofCustomFunction(CustomFunction cf) {
        var cfName = cf.name();
        var cfArgs = new Args(cf.parameterTypes());
        var cfExecutor = new CustomFunctionExecutor(cf.function());
        return new Invoker(cfName, cfArgs, cfExecutor);
    }

    /// Constructs an `Invokers` instance, grouping and mapping invokers by their names and argument types to their
    /// corresponding executors.
    ///
    /// @param invokerStream a stream of `Invoker` objects, where each invoker encapsulates the method name, its
    ///         parameter types, and the associated method executor.
    public Invokers(Stream<Invoker> invokerStream) {
        map = invokerStream.collect(groupingBy(Invoker::name, toMap(Invoker::args, Invoker::executor)));
    }

    /// Transforms a map containing interface-to-implementation mappings into a stream of `Invoker` objects. Each entry
    /// in the map is processed to generate a flat stream of relevant `Invoker` instances.
    ///
    /// @param interfaces2implementations a map where keys represent interface classes and values represent
    ///         their corresponding implementations, used to derive invoker instances.
    ///
    /// @return a stream of `Invoker` objects derived from the provided map entries.
    public static Stream<Invoker> streamInvokers(Map<Class<?>, ?> interfaces2implementations) {
        return interfaces2implementations.entrySet()
                                         .stream()
                                         .flatMap(Invokers::streamInvokers);
    }

    private static Stream<Invoker> streamInvokers(Entry<Class<?>, ?> interface2implementation) {
        return streamInvokers(interface2implementation.getKey(), interface2implementation.getValue());
    }

    private static Stream<Invoker> streamInvokers(Class<?> key, Object obj) {
        return stream(key.getDeclaredMethods()).map(method -> new Invoker(obj, method));
    }

    /// Resolves a method executor for a given method name and argument types within the specified context and target
    /// object. This method attempts to find a matching executor for methods registered with a specific name and
    /// compatible argument types.
    ///
    /// @param context the evaluation context in which the method is being resolved, providing necessary state
    ///         and configuration.
    /// @param targetObject the object on which the resolved method will be invoked.
    /// @param name the name of the method to resolve.
    /// @param argumentTypes a list of type descriptors representing the argument types of the method to
    ///         resolve.
    ///
    /// @return the resolved [MethodExecutor] if a compatible method is found; otherwise, returns `null`.
    @Override
    @Nullable
    public MethodExecutor resolve(
            EvaluationContext context,
            Object targetObject,
            String name,
            List<TypeDescriptor> argumentTypes
    ) {
        var argumentClasses = argumentTypes.stream()
                                           .map(this::typeDescriptor2Class)
                                           .toList();
        return map.getOrDefault(name, emptyMap())
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getKey()
                                        .validate(argumentClasses))
                  .map(Entry::getValue)
                  .findFirst()
                  .orElse(null);
    }

    @SuppressWarnings("rawtypes")
    private Class typeDescriptor2Class(@Nullable TypeDescriptor typeDescriptor) {
        // When null, consider it as compatible with any type argument, so return Any.class placeholder
        return typeDescriptor == null ? Any.class : typeDescriptor.getType();
    }

    /// This class represents a placeholder validating all other classes as possible candidate for validation. It is not
    /// supposed to be instantiated.
    private interface Any {

    }

    /// Represents argument types associated with method invocation.
    ///
    /// This record encapsulates a list of parameter types and provides a method to validate whether a list of target
    /// types matches the source types.
    ///
    /// The validation logic ensures that each target type is compatible with the corresponding source type. A type is
    /// compatible if it matches precisely or is assignable from the source type. Additionally, the `Any` class acts as
    /// a wildcard placeholder, making any type compatible.
    ///
    /// @param sourceTypes a list of parameter types representing the method's signature.
    public record Args(List<Class<?>> sourceTypes) {

        /// Validates if the provided list of classes matches the source types according to the compatibility rules. A
        /// type is considered compatible if it matches precisely or is assignable from the corresponding source type.
        /// Additionally, the `Any` class serves as a wildcard, making any type compatible.
        ///
        /// @param searchedTypes the list of classes to validate against the source types.
        ///
        /// @return true if all the searched classes are compatible with the source types; false otherwise.
        @SuppressWarnings("rawtypes")
        public boolean validate(List<Class> searchedTypes) {
            if (searchedTypes.size() != sourceTypes.size()) return false;

            var sourceTypesQ = new ArrayDeque<>(sourceTypes);
            var searchedTypesQ = new ArrayDeque<>(searchedTypes);
            var valid = true;
            while (!sourceTypesQ.isEmpty() && valid) {
                Class<?> parameterType = sourceTypesQ.remove();
                Class<?> searchedType = searchedTypesQ.remove();
                valid = searchedType == Any.class || parameterType.isAssignableFrom(searchedType);
            }
            return valid;
        }
    }

    /// Encapsulates a custom function as a method executor, allowing the execution of the function with a list of
    /// arguments in a given evaluation context.
    ///
    /// This class implements the `MethodExecutor` interface from the Spring Expression framework.
    private record CustomFunctionExecutor(Function<List<Object>, Object> function)
            implements MethodExecutor {

        /// Executes the method with the provided evaluation context, target object, and arguments.
        ///
        /// The method applies the encapsulated function to the arguments and returns the result as a TypedValue.
        ///
        /// @param context the evaluation context in which the method is executed.
        /// @param target the target object on which the method is invoked, if applicable.
        /// @param arguments the arguments to be passed to the method during execution.
        ///
        /// @return the result of the method execution encapsulated in a TypedValue.
        @Override
        public TypedValue execute(EvaluationContext context, Object target, Object... arguments) {
            return new TypedValue(function.apply(asList(arguments)));
        }
    }
}
