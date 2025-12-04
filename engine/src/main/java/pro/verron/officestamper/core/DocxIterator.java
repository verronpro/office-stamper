package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.DocxPart;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
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
    private Object next;

    private DocxIterator(ContentAccessor contentAccessor) {
        this(contentAccessor.getContent()::iterator);
    }

    private DocxIterator(Supplier<Iterator<Object>> supplier) {
        this.supplier = supplier;
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    /// Creates a [ResetableIterator] of [CommentRangeStart] instances from the given [WordprocessingMLPackage]
    /// document. This method leverages a [DocxIterator] to iterate through the contents of the specified document part
    /// and filters for [CommentRangeStart] elements.
    ///
    /// @param contentAccessor a [ContentAccessor] used to access the content within the specified part
    ///
    /// @return a [ResetableIterator] containing the [CommentRangeStart] elements found in the provided content
    public static ResetableIterator<CommentRangeStart> ofCRS(ContentAccessor contentAccessor) {
        var iterator = new DocxIterator(contentAccessor);
        return new FilterMapperIterator<>(iterator, CommentRangeStart.class::isInstance, CommentRangeStart.class::cast);
    }

    public static ResetableIterator<R> ofRun(ContentAccessor contentAccessor) {
        var iterator = new DocxIterator(contentAccessor);
        return new FilterMapperIterator<>(iterator, R.class::isInstance, R.class::cast);
    }

    public static ResetableIterator<Tag> ofTags(ContentAccessor contentAccessor, DocxPart part) {
        var iterator = new DocxIterator(contentAccessor);
        var element = "officestamper";
        Predicate<Object> predicate = o -> o instanceof CTSmartTagRun tag  //
                                           && Hook.isTagElement(tag, element);
        Function<Object, CTSmartTagRun> caster = CTSmartTagRun.class::cast;
        Function<Object, Tag> mapper = caster.andThen((CTSmartTagRun tag) -> Tag.of(part, tag));
        return new FilterMapperIterator<>(iterator, predicate, mapper);
    }

    public static ResetableIterator<Optional<Hook>> ofHooks(ContentAccessor contentAccessor, DocxPart part) {
        var iterator = new DocxIterator(contentAccessor);
        return new FilterMapperIterator<>(iterator, Hook.filter(part));
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
                var content = sdtRun.getSdtContent()
                                    .getContent();
                iteratorQueue.add(content.iterator());
            }
            case SdtBlock sdtBlock -> {
                var content = sdtBlock.getSdtContent()
                                      .getContent();
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
            if (nextIterator == null) break;
            if (nextIterator.hasNext()) {
                next = unwrap(nextIterator.next());
                iteratorQueue.add(nextIterator);
            }
        }
        return result;
    }

    @Override
    public void reset() {
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }
}
