package pro.verron.officestamper.core;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.*;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * Resolves methods that are used as expression functions or comment processors.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.2
 */
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

    /** {@inheritDoc} */
    @Override public MethodExecutor resolve(
            @NonNull EvaluationContext context,
            @NonNull Object targetObject,
            @NonNull String name,
            @NonNull List<TypeDescriptor> argumentTypes
    ) {
        List<Class<?>> argumentClasses = new ArrayList<>();
        argumentTypes.forEach(at -> argumentClasses.add(at.getType()));
        return map.getOrDefault(name, emptyMap())
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getKey()
                                        .validate(argumentClasses))
                  .map(Entry::getValue)
                  .findFirst()
                  .orElse(null);
    }

    public record Args(List<Class<?>> sourceTypes) {
        public boolean validate(List<Class<?>> searchedTypes) {
            if (searchedTypes.size() != sourceTypes.size()) return false;

            var sourceTypesQ = new ArrayDeque<>(sourceTypes);
            var searchedTypesQ = new ArrayDeque<>(searchedTypes);
            var valid = true;
            while (!sourceTypesQ.isEmpty() && valid) {
                Class<?> parameterType = sourceTypesQ.remove();
                Class<?> searchedType = searchedTypesQ.remove();
                valid = parameterType.isAssignableFrom(searchedType);
            }
            return valid;
        }
    }
}
