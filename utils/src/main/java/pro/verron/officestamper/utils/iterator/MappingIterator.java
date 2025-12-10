package pro.verron.officestamper.utils.iterator;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.Function;

/// An iterator that filters and maps elements from another ResetableIterator based on user-provided filter and mapper
/// functions.
///
/// This class allows elements to be iterated through with specific filtering and transformation logic applied. The
/// iterator can also be reset to its initial state, re-evaluating the elements based on the same logic.
///
/// @param <S> the type of the source elements in the parent iterator
/// @param <T> the type of the transformed elements returned by this iterator
public class MappingIterator<S, T>
        implements ResetableIterator<T> {
    private final Function<S, T> mapper;
    private final ResetableIterator<S> source;
    @Nullable T next;

    /// Constructs a MappingIterator with a parent iterator and a mapping function.
    ///
    /// @param source the underlying ResetableIterator containing the source elements
    /// @param mapper a function to transform the elements into a different type
    public MappingIterator(ResetableIterator<S> source, Function<S, T> mapper) {
        this.source = source;
        this.mapper = mapper;
        findNext();
    }

    private void findNext() {
        next = null;
        while (source.hasNext() && next == null) {
            var o = source.next();
            next = mapper.apply(o);
        }
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
