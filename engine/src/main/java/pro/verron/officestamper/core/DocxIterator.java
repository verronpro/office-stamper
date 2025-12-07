package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.DocxPart;

import java.util.*;
import java.util.function.Supplier;

import static org.docx4j.XmlUtils.unwrap;
import static pro.verron.officestamper.core.Hook.isTagElement;

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
        return new DocxIterator(contentAccessor).filter(CommentRangeStart.class::isInstance)
                                                .map(CommentRangeStart.class::cast);
    }

    public static ResetableIterator<R> ofRun(ContentAccessor contentAccessor) {
        return new DocxIterator(contentAccessor).filter(R.class::isInstance)
                                                .map(R.class::cast);

    }

    public static ResetableIterator<Hook> ofHooks(ContentAccessor contentAccessor, DocxPart part) {
        return new DocxIterator(contentAccessor).filter(DocxIterator::isPotentialHook)
                                                .map(o -> asHook(part, o));
    }

    private static boolean isPotentialHook(Object o) {
        return switch (o) {
            case CommentRangeStart _ -> true;
            case CTSmartTagRun tag when isTagElement(tag, "officestamper") -> true;
            default -> false;
        };
    }

    private static Hook asHook(DocxPart part, Object o) {
        return switch (o) {
            case CommentRangeStart commentRangeStart -> Hook.newCommentHook(part, commentRangeStart);
            case CTSmartTagRun tag -> Hook.newTagHook(part, tag);
            default -> throw new IllegalArgumentException("Unexpected value: " + o);
        };
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
