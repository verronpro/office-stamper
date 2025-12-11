package pro.verron.officestamper.utils.wml;

import org.docx4j.wml.ContentAccessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static pro.verron.officestamper.utils.wml.WmlFactory.*;

class DocxIteratorTest {
    @Test
    @DisplayName("hasNext returns false when there are no elements")
    void testHasNextReturnsFalseWhenNoElements() {
        var iterator = new DocxIterator(Collections::emptyList);
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("hasNext returns true when there is at least one element")
    void testHasNextReturnsTrueWhenThereIsElement() {
        var obj = new Object();
        var iterator = new DocxIterator(() -> List.of(obj));
        assertTrue(iterator.hasNext());
    }

    @Test
    @DisplayName("next throws exception when no more elements")
    void testNextThrowsExceptionWhenNoMoreElements() {
        var iterator = new DocxIterator(Collections::emptyList);
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    @DisplayName("next returns correct object in flat structure")
    void testNextReturnsCorrectFlatObject() {
        var obj = new Object();
        var iterator = new DocxIterator(() -> List.of(obj));

        assertTrue(iterator.hasNext());
        assertSame(obj, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("next handles nested ContentAccessor properly")
    void testNextHandlesNestedContentAccessor() {
        var innerObj = new Object();
        ContentAccessor childContent = () -> List.of(innerObj);
        ContentAccessor contentAccessor = () -> List.of(childContent);
        var iterator = new DocxIterator(contentAccessor);

        assertEquals(childContent, iterator.next());
        assertEquals(innerObj, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("next handles SdtRun structure properly")
    void testNextHandlesSdtRunStructure() {
        var innerObj = new Object();
        var sdtRun = newSdtRun(innerObj);
        var iterator = new DocxIterator(() -> List.of(sdtRun));

        assertSame(sdtRun, iterator.next());
        assertSame(innerObj, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("next handles SdtBlock structure properly")
    void testNextHandlesSdtBlockStructure() {
        var innerObj = new Object();
        var sdtBlock = newSdtBlock(innerObj);
        var iterator = new DocxIterator(() -> List.of(sdtBlock));

        assertSame(sdtBlock, iterator.next());
        assertSame(innerObj, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("next handles Pict structure properly")
    void testNextHandlesPictStructure() {
        var innerObj = new Object();
        var pict = newPict(innerObj);
        var iterator = new DocxIterator(() -> List.of(pict));

        assertSame(pict, iterator.next());
        assertSame(innerObj, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("reset resets iterator to initial state")
    void testResetResetsToInitialState() {
        var firstObj = new Object();
        var secondObj = new Object();
        var iterator = new DocxIterator(() -> List.of(firstObj, secondObj));

        // First pass
        assertSame(firstObj, iterator.next());
        assertSame(secondObj, iterator.next());
        assertFalse(iterator.hasNext());

        // Reset and check again
        iterator.reset();
        assertTrue(iterator.hasNext());
        assertSame(firstObj, iterator.next());
        assertSame(secondObj, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("selectClass filters elements by class type")
    void testSelectClassFiltersElementsByType() {
        var str1 = "hello";
        var num1 = 42;
        var str2 = "world";
        var iterator = new DocxIterator(() -> List.of(str1, num1, str2));
        var stringIterator = iterator.selectClass(String.class);

        assertTrue(stringIterator.hasNext());
        assertEquals("hello", stringIterator.next());
        assertTrue(stringIterator.hasNext());
        assertEquals("world", stringIterator.next());
        assertFalse(stringIterator.hasNext());
    }

    @Test
    @DisplayName("Complex nested structure with multiple levels")
    void testComplexNestedStructure() {
        var innermostObj = new Object();
        ContentAccessor innerContent = () -> List.of(innermostObj);
        var sdtBlock = newSdtBlock(innerContent);
        var topLevelObj = new Object();
        var iterator = new DocxIterator(() -> List.of(topLevelObj, sdtBlock));

        assertSame(topLevelObj, iterator.next());
        assertSame(sdtBlock, iterator.next());
        assertSame(innerContent, iterator.next());
        assertSame(innermostObj, iterator.next());
        assertFalse(iterator.hasNext());
    }
}
