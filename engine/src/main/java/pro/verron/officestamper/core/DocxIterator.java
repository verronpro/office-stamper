package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Tag;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.docx4j.XmlUtils.unwrap;

/// An iterator that allows the traversal of objects within a WordprocessingML-based document part.
/// The iterator supports nested structures, enabling iteration over content that may have hierarchical data,
/// like paragraphs, structured document tags (SDTs), and runs.
///
/// This class implements the [ResetableIterator] interface, allowing for the iteration to be reset
/// to its initial state, ensuring reusability of the same iterator instance.
public class DocxIterator
        implements ResetableIterator<Object> {

    private final Supplier<Iterator<Object>> supplier;
    private Queue<Iterator<?>> iteratorQueue;
    private Object next;

    private DocxIterator(Supplier<Iterator<Object>> supplier) {
        this.supplier = supplier;
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    /// Creates a [ResetableIterator] of [StandardParagraph] instances from the given [DocxPart].
    /// Extracts [P] or [CTSdtContentRun] elements from the [DocxPart] and maps them to [StandardParagraph]
    ///  objects.
    ///
    /// @param docxPart the [DocxPart] object from which paragraphs will be extracted
    ///
    /// @return a [ResetableIterator] containing the extracted and mapped [StandardParagraph] instances
    public static ResetableIterator<StandardParagraph> ofParagraphs(DocxPart docxPart) {
        var iterator = new DocxIterator(() -> docxPart.content()
                                                      .iterator());
        Predicate<Object> isParagraph = P.class::isInstance;
        Predicate<Object> isSdtRun = CTSdtContentRun.class::isInstance;
        var predicate = isParagraph.or(isSdtRun);
        Function<Object, StandardParagraph> mapper = o -> switch (o) {
            case P p -> StandardParagraph.from(docxPart, p);
            case CTSdtContentRun ctSdtContentRun -> StandardParagraph.from(docxPart, ctSdtContentRun);
            default -> throw new OfficeStamperException("Unexpected element type: " + o.getClass());
        };
        return new FilterMapperIterator<>(iterator, predicate, mapper);
    }

    /// Creates a [ResetableIterator] of [CommentRangeStart] instances from the given [WordprocessingMLPackage]
    /// document.
    /// This method leverages a [DocxIterator] to iterate through the contents of the specified document part
    /// and filters for [CommentRangeStart] elements.
    ///
    /// @param contentAccessor a [ContentAccessor] used to access the content within the specified part
    ///
    /// @return a [ResetableIterator] containing the [CommentRangeStart] elements found in the provided content
    public static ResetableIterator<CommentRangeStart> ofCRS(ContentAccessor contentAccessor) {
        var iterator = new DocxIterator(contentAccessor.getContent()::iterator);
        return new FilterMapperIterator<>(iterator, CommentRangeStart.class::isInstance, CommentRangeStart.class::cast);
    }

    public static ResetableIterator<R> ofRun(ContentAccessor contentAccessor) {
        var iterator = new DocxIterator(contentAccessor.getContent()::iterator);
        return new FilterMapperIterator<>(iterator, R.class::isInstance, R.class::cast);
    }

    public static ResetableIterator<Tag> ofTags(ContentAccessor contentAccessor, DocxPart docxPart) {
        var iterator = new DocxIterator(() -> contentAccessor.getContent()
                                                             .iterator());
        var element = "officestamper";
        Predicate<Object> predicate = o -> o instanceof CTSmartTagRun tag  //
                                           && isTagElement(tag, element);
        Function<Object, CTSmartTagRun> caster = CTSmartTagRun.class::cast;
        Function<Object, Tag> mapper = caster.andThen((CTSmartTagRun tag) -> Tag.of(docxPart, tag));
        return new FilterMapperIterator<>(iterator, predicate, mapper);
    }

    private static boolean isTagElement(CTSmartTagRun tag, String element) {
        var actualElement = tag.getElement();
        var expectedElement = element;
        return Objects.equals(expectedElement, actualElement);
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
