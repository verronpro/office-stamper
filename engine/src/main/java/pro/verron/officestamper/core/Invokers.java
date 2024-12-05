package pro.verron.officestamper.core;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/// Resolves methods used as expression functions or comment processors.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.2
public class Invokers
        implements MethodResolver {
    private final Map<String, Map<Args, MethodExecutor>> map;

    public Invokers(Stream<Invoker> invokerStream) {
        map = invokerStream.collect(groupingBy(Invoker::name, toMap(Invoker::args, Invoker::executor)));
    }

    static Stream<Invoker> streamInvokers(Map<Class<?>, ?> interfaces2implementations) {
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

    @Override
    public MethodExecutor resolve(
            @NonNull EvaluationContext context,
            @NonNull Object targetObject,
            @NonNull String name,
            @NonNull List<TypeDescriptor> argumentTypes
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

    /// When null, consider it as compatible with any type argument, so return Any.class placeholder
    private Class typeDescriptor2Class(@Nullable TypeDescriptor typeDescriptor) {
        return typeDescriptor == null ? Any.class : typeDescriptor.getType();
    }

    public record Args(List<Class<?>> sourceTypes) {
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

    /// Represent a placeholder validating all other classes as possible candidate for validation
    private class Any {}
}
