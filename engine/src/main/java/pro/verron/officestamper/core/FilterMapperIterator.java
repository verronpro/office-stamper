package pro.verron.officestamper.core;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/// An iterator that filters and maps elements from another ResetableIterator
/// based on user-provided filter and mapper functions.
///
/// This class allows elements to be iterated through with specific filtering
/// and transformation logic applied. The iterator can also be reset to its
/// initial state, re-evaluating the elements based on the same logic.
///
/// @param <S> the type of the source elements in the parent iterator
/// @param <T> the type of the transformed elements returned by this iterator
public class FilterMapperIterator<S, T>
        implements ResetableIterator<T> {
    private final Predicate<S> filter;
    private final Function<S, T> mapper;
    private final ResetableIterator<S> source;
    T next;

    public FilterMapperIterator(ResetableIterator<S> source, Class<T> clazz) {
        this(source, clazz::isInstance, clazz::cast);
    }

    /// Constructs a FilterMapperIterator with a parent iterator, a filtering condition, and a mapping function.
    ///
    /// @param source the underlying ResetableIterator containing the source elements
    /// @param filter a predicate to filter elements from the parent iterator based on specific conditions
    /// @param mapper a function to transform the filtered elements into a different type
    public FilterMapperIterator(ResetableIterator<S> source, Predicate<S> filter, Function<S, T> mapper) {
        this.source = source;
        this.filter = filter;
        this.mapper = mapper;
        findNext();
    }

    private void findNext() {
        next = null;
        while (source.hasNext() && next == null) {
            var o = source.next();
            if (filter.test(o)) next = mapper.apply(o);
        }
    }

    public <O> FilterMapperIterator<T, O> refilter(Predicate<T> filter, Function<T, O> mapper) {
        return new FilterMapperIterator<>(this, filter, mapper);
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        if (next == null) throw new NoSuchElementException("No more elements to iterate");
        T result = next;
        findNext();
        return result;
    }

    @Override
    public void reset() {
        source.reset();
        findNext();
    }
}
