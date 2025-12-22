package pro.verron.officestamper.utils.iterator;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;

/// A [ResetableIterator] implementation that provides a sliced view of another iterator. It allows iteration over a
/// subset of elements from a source iterator, defined by a start and end element.
///
/// @param <T> the type of elements returned by this iterator
public class SlicingIterator<T>
        implements ResetableIterator<T> {
    private final ResetableIterator<T> source;
    private final T start;
    private final T end;
    private @Nullable T next;
    private boolean foundStart;


    /// Constructs a new SlicingIterator with the specified source iterator and boundaries.
    ///
    /// @param source the underlying [ResetableIterator] to slice
    /// @param start the starting element (inclusive) for iteration
    /// @param end the ending element (inclusive) for iteration
    public SlicingIterator(ResetableIterator<T> source, T start, T end) {
        this.source = source;
        this.start = start;
        this.end = end;
        findNext();
    }

    private void findNext() {
        next = null;
        while (source.hasNext() && (!foundStart || (next == end || next == null))) {
            next = source.next();
            if (next == start) foundStart = true;
        }
    }

    @Override
    public void reset() {
        source.reset();
        findNext();
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
}
