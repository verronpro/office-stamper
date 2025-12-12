package pro.verron.officestamper.utils.iterator;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

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


    /// Returns a new [ResetableIterator] that filters elements based on the provided predicate.
    ///
    /// This method creates a new iterator that wraps the current iterator and applies the given predicate to each
    /// element. Only elements that satisfy the predicate (i.e., for which the predicate returns `true`) will be
    /// included in the iteration.
    ///
    /// @param predicate a [Predicate] used to determine which elements to include in the filtered iterator
    ///
    /// @return a new [ResetableIterator] instance that provides only the elements matching the predicate
    default ResetableIterator<T> filter(Predicate<T> predicate) {
        return new FilteringIterator<>(this, predicate);
    }

    /// Returns a new [ResetableIterator] that applies the given function to each element.
    ///
    /// This method creates a new iterator that wraps the current iterator and applies the provided function to
    /// transform each element. The resulting iterator will yield the transformed elements.
    ///
    /// @param function a [Function] used to transform each element
    /// @param <U> the type of elements returned by the function and the resulting iterator
    ///
    /// @return a new [ResetableIterator] instance that provides the transformed elements
    default <U> ResetableIterator<U> map(Function<T, U> function) {
        return new MappingIterator<>(this, function);
    }


    /// Collects the elements of this iterator into a container using the provided collector.
    ///
    /// This method creates a stream from the iterator's elements and collects them using the specified collector. It
    /// allows for flexible reduction operations such as collecting into lists, sets, maps, or performing other
    /// aggregation operations.
    ///
    /// @param collector the [Collector] used to accumulate elements into a result container
    /// @param <R> the type of the result container
    ///
    /// @return the result of the collection operation
    default <R> R collect(Collector<? super T, ?, R> collector) {
        var spliterator = Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false)
                            .collect(collector);
    }
}
