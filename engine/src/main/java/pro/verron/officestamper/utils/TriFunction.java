package pro.verron.officestamper.utils;

/**
 * Represents a functional interface that takes three input arguments of types T, U, and V
 * and produces a result of type W. This is a tri-arity specialization of a function, allowing
 * operations on three inputs to produce a single output.
 *
 * @param <T> the type of the first input to the function
 * @param <U> the type of the second input to the function
 * @param <V> the type of the third input to the function
 * @param <W> the type of the function result
 */
@FunctionalInterface
public interface TriFunction<T, U, V, W> {
    /**
     * Applies this function to the given arguments and produces a result.
     *
     * @param t the first input argument to the function
     * @param u the second input argument to the function
     * @param v the third input argument to the function
     * @return the result produced by applying this function to the given arguments
     */
    W apply(T t, U u, V v);
}
