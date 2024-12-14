package pro.verron.officestamper.api;

import pro.verron.officestamper.utils.TriFunction;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public record CustomFunction(
        String name,
        List<Class<?>> parameterTypes,
        Function<List<Object>, Object> function
) {
    public interface NeedsFunctionImpl<T> {
        void withImplementation(Function<T, ?> function);
    }

    public interface NeedsBiFunctionImpl<T, U> {
        void withImplementation(BiFunction<T, U, ?> object);
    }

    public interface NeedsTriFunctionImpl<T, U, V> {
        void withImplementation(TriFunction<T, U, V, ?> function);
    }
}
