package pro.verron.officestamper.core;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Filter<S, T> {
    static <S, T> Filter<S, T> simple(Predicate<S> filter, Function<S, T> mapper) {
        return new Filter<>() {
            @Override
            public Predicate<S> filter() {
                return filter;
            }

            @Override
            public Function<S, T> mapper() {
                return mapper;
            }
        };
    }

    Predicate<S> filter();

    Function<S, T> mapper();
}
