package pro.verron.officestamper.utils.iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests the {@link ResetableIterator} class.
public class ResetableIteratorTest {

    private ResetableIterator<Integer> iterator;

    /// Default constructor.
    public ResetableIteratorTest() {
    }

    @BeforeEach
    void setUp() {
        List<Integer> testData = Arrays.asList(1, 2, 3, 4, 5);
        iterator = new TestResetableIterator<>(testData);
    }

    /// Tests the reset functionality of the ResetableIterator.
    ///
    /// Given a ResetableIterator with some data, when we iterate through part of it and then call reset(), then we
    /// should be able to iterate again from the beginning.
    @Test
    @DisplayName("Resetting an iterator")
    void testReset() {
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next());

        iterator.reset();

        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next()); // Should start from the beginning after reset
    }

    /// Tests the filter method of the ResetableIterator.
    ///
    /// Given a ResetableIterator with integer data, when we apply a filter to keep only even numbers, then the
    /// resulting iterator should contain only those even numbers.
    @Test
    @DisplayName("Filtering an iterator")
    void testFilter() {
        Predicate<Integer> isEven = x -> x % 2 == 0;
        ResetableIterator<Integer> filteredIterator = iterator.filter(isEven);

        List<Integer> result = new ArrayList<>();
        filteredIterator.forEachRemaining(result::add);

        assertEquals(Arrays.asList(2, 4), result);
    }

    /// Tests the map method of the ResetableIterator.
    ///
    /// Given a ResetableIterator with integer data, when we apply a mapping function that doubles each number, then the
    /// resulting iterator should yield doubled values.
    @Test
    @DisplayName("Mapping an iterator")
    void testMap() {
        ResetableIterator<String> mappedIterator = iterator.map(Object::toString);

        List<String> result = new ArrayList<>();
        mappedIterator.forEachRemaining(result::add);

        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), result);
    }

    /// Tests combined usage of filter and map methods.
    ///
    /// Given a ResetableIterator with integer data, when we first filter for even numbers and then map them to strings,
    /// then the resulting iterator should yield string representations of even numbers.
    @Test
    void testFilterAndMapCombined() {
        Predicate<Integer> isEven = x -> x % 2 == 0;
        ResetableIterator<String> processedIterator = iterator.filter(isEven)
                                                              .map(Object::toString);

        List<String> result = new ArrayList<>();
        processedIterator.forEachRemaining(result::add);

        assertEquals(Arrays.asList("2", "4"), result);
    }

    /// Tests resetting a filtered iterator.
    ///
    /// Given a filtered ResetableIterator, when we consume all elements and then reset it, then we should be able to
    /// iterate again from the beginning.
    @Test
    void testFilteredIteratorReset() {
        ResetableIterator<Integer> filteredIterator = iterator.filter(x -> x > 3);

        // First iteration
        List<Integer> firstPass = new ArrayList<>();
        filteredIterator.forEachRemaining(firstPass::add);
        assertEquals(Arrays.asList(4, 5), firstPass);

        // Reset and second iteration
        filteredIterator.reset();

        List<Integer> secondPass = new ArrayList<>();
        filteredIterator.forEachRemaining(secondPass::add);
        assertEquals(Arrays.asList(4, 5), secondPass);
    }

    /// Tests resetting a mapped iterator.
    ///
    /// Given a mapped ResetableIterator, when we consume all elements and then reset it, then we should be able to
    /// iterate again from the beginning.
    @Test
    void testMappedIteratorReset() {
        ResetableIterator<String> mappedIterator = iterator.map(i -> "Num" + i);

        // First iteration
        List<String> firstPass = new ArrayList<>();
        mappedIterator.forEachRemaining(firstPass::add);
        assertEquals(Arrays.asList("Num1", "Num2", "Num3", "Num4", "Num5"), firstPass);

        // Reset and second iteration
        mappedIterator.reset();

        List<String> secondPass = new ArrayList<>();
        mappedIterator.forEachRemaining(secondPass::add);
        assertEquals(Arrays.asList("Num1", "Num2", "Num3", "Num4", "Num5"), secondPass);
    }

    private static class TestResetableIterator<T>
            implements ResetableIterator<T> {
        private final List<T> data;
        private int index = 0;

        public TestResetableIterator(List<T> data) {
            this.data = new ArrayList<>(data);
        }

        @Override
        public void reset() {
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < data.size();
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            return data.get(index++);
        }
    }
}
