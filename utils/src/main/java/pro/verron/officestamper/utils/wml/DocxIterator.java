package pro.verron.officestamper.utils.wml;

import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Pict;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtRun;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.utils.iterator.ResetableIterator;

import java.util.*;
import java.util.function.Supplier;

import static org.docx4j.XmlUtils.unwrap;

/// An iterator that allows the traversal of objects within a WordprocessingML-based document part. The iterator
/// supports nested structures, enabling iteration over content that may have hierarchical data, like paragraphs,
/// structured document tags (SDTs), and runs.
///
/// This class implements the [ResetableIterator] interface, allowing for the iteration to be reset to its initial
/// state, ensuring reusability of the same iterator instance.
public class DocxIterator
        implements ResetableIterator<Object> {

    private final Supplier<Iterator<Object>> supplier;
    private Queue<Iterator<?>> iteratorQueue;
    private @Nullable Object next;

    /// Creates a new [DocxIterator] instance that iterates over the content of the given [ContentAccessor].
    ///
    /// @param contentAccessor the content accessor whose content will be iterated over
    public DocxIterator(ContentAccessor contentAccessor) {
        this(contentAccessor.getContent()::iterator);
    }

    private DocxIterator(Supplier<Iterator<Object>> supplier) {
        this.supplier = supplier;
        initialize();
    }

    private void initialize() {
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    /// Selects and casts elements of the specified class type from the iterator.
    ///
    /// @param aClass the class type to filter and cast elements to
    /// @param <T> the type of elements to select
    ///
    /// @return a new [ResetableIterator] containing only elements of the specified class type
    public <T> ResetableIterator<T> selectClass(Class<T> aClass) {
        return filter(aClass::isInstance).map(aClass::cast);
    }

    @Override
    public void reset() {
        initialize();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Object next() {
        if (next == null) throw new NoSuchElementException("No more elements to iterate");

        var result = next;

        next = null;
        switch (result) {
            case ContentAccessor contentAccessor -> {
                var content = contentAccessor.getContent();
                iteratorQueue.add(content.iterator());
            }
            case SdtRun sdtRun -> {
                var sdtContent = sdtRun.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(content.iterator());
            }
            case SdtBlock sdtBlock -> {
                var sdtContent = sdtBlock.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(content.iterator());
            }
            case Pict pict -> {
                var content = pict.getAnyAndAny();
                iteratorQueue.add(content.iterator());
            }
            default -> { /* DO NOTHING */ }
        }
        while (!iteratorQueue.isEmpty() && next == null) {
            var nextIterator = iteratorQueue.poll();
            if (nextIterator.hasNext()) {
                next = unwrap(nextIterator.next());
                iteratorQueue.add(nextIterator);
            }
        }
        return result;
    }
}
