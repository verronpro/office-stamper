package pro.verron.officestamper.core;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/// An interface that extends the [Iterator] interface, providing an additional capability to reset the iterator back to
/// its initial state.
///
/// This interface is useful in scenarios where an iteration process needs to be repeated or restarted without creating
/// a new instance of the iterator. The `reset` method ensures that the iterator can be reused and revisits the elements
/// starting from the beginning.
///
/// @param <T> the type of elements returned by this iterator
public interface ResetableIterator<T>
        extends Iterator<T> {
    /// Resets the iterator to its initial state, allowing for iteration to start over from the beginning.
    ///
    /// This method is intended for scenarios where the same iteration process needs to be repeated multiple times
    /// without recreating a new instance of the iterator. After calling this method, the iterator should behave as
    /// though it was freshly initialized, with any internal state reverted to its starting condition.
    void reset();

    default ResetableIterator<T> filter(Predicate<T> predicate) {
        return new FilteringIterator<>(this, predicate);
    }

    default <U> ResetableIterator<U> map(Function<T, U> function) {
        return new MappingIterator<>(this, function);
    }
}
