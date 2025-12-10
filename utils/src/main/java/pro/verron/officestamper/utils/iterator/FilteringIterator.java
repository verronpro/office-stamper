package pro.verron.officestamper.utils.iterator;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

/// An iterator that filters elements from another ResetableIterator based on user-provided filter functions.
///
/// This class allows elements to be iterated through with specific filtering applied. The iterator can also be reset to
/// its initial state, re-evaluating the elements based on the same logic.
///
/// @param <T> the type of the source elements in the parent iterator
public class FilteringIterator<T>
        implements ResetableIterator<T> {
    private final Predicate<T> filter;
    private final ResetableIterator<T> source;
    @Nullable T next;

    /// Constructs a MappingIterator with a parent iterator and a mapping function.
    ///
    /// @param source the underlying ResetableIterator containing the source elements
    /// @param mapper a function to transform the elements into a different type
    public FilteringIterator(ResetableIterator<T> source, Predicate<T> mapper) {
        this.source = source;
        this.filter = mapper;
        findNext();
    }

    private void findNext() {
        next = null;
        while (source.hasNext() && next == null) {
            var o = source.next();
            if (filter.test(o)) next = o;
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
