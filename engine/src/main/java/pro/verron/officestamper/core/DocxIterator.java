package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.docx4j.XmlUtils.unwrap;

/// An iterator that allows the traversal of objects within a WordprocessingML-based document part.
/// The iterator supports nested structures, enabling iteration over content that may have hierarchical data,
/// like paragraphs, structured document tags (SDTs), and runs.
///
/// This class implements the [ResetableIterator] interface, allowing for the iteration to be reset
/// to its initial state, ensuring reusability of the same iterator instance.
public class DocxIterator
        implements ResetableIterator<Object> {

    private final DocxPart docxPart;
    private Queue<Iterator<?>> iteratorQueue;
    private Object next;

    private DocxIterator(DocxPart docxPart) {
        this.docxPart = docxPart;
        var startingIterator = docxPart.content()
                                       .iterator();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    /// Creates a [ResetableIterator] of [StandardParagraph] instances from the given [DocxPart].
    /// Extracts [P] or [CTSdtContentRun] elements from the [DocxPart] and maps them to [StandardParagraph]
    ///  objects.
    ///
    /// @param docxPart the [DocxPart] object from which paragraphs will be extracted
    /// @return a [ResetableIterator] containing the extracted and mapped [StandardParagraph] instances
    public static ResetableIterator<StandardParagraph> ofParagraphs(DocxPart docxPart) {
        var iterator = new DocxIterator(docxPart);
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

    /// Creates a [ResetableIterator] of [CommentRangeStart] instances from the given [WordprocessingMLPackage] document.
    /// This method leverages a [DocxIterator] to iterate through the contents of the specified document part
    /// and filters for [CommentRangeStart] elements.
    ///
    /// @param document the [WordprocessingMLPackage] that contains the document structure and content
    /// @param part the specific part of the [WordprocessingMLPackage] to be processed
    /// @param contentAccessor a [ContentAccessor] used to access the content within the specified part
    /// @return a [ResetableIterator] containing the [CommentRangeStart] elements found in the provided content
    public static ResetableIterator<CommentRangeStart> ofCRS(WordprocessingMLPackage document,
            Part part,
            ContentAccessor contentAccessor) {
        var iterator = new DocxIterator(new TextualDocxPart(document, part, contentAccessor));
        return new FilterMapperIterator<>(iterator, CommentRangeStart.class::isInstance, CommentRangeStart.class::cast);
    }

    public static Iterator<R> ofRun(ContentAccessor contentAccessor) {
        var iterator = new DocxIterator(new TextualDocxPart(contentAccessor));
        return new FilterMapperIterator<>(iterator, R.class::isInstance, R.class::cast);
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
                var content = sdtBlock.getSdtContent().getContent();
                iteratorQueue.add(content.iterator());
            }
            case Pict pict -> {
                var content = pict.getAnyAndAny();
                iteratorQueue.add(content.iterator());
            }
            default -> {
            }
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
        var startingIterator = docxPart.content()
                                       .iterator();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }
}
