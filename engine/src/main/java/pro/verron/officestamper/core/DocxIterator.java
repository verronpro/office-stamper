package pro.verron.officestamper.core;

import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtRun;
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
        this.next = startingIterator.hasNext() ? startingIterator.next() : null;
    }

    /// Creates a ResetableIterator of StandardParagraph instances from the given DocxPart.
    /// Extracts elements of type P or CTSdtContentRun from the DocxPart and maps them to StandardParagraph objects.
    ///
    /// @param docxPart the DocxPart object from which paragraphs will be extracted
    /// @return a [ResetableIterator] containing the extracted and mapped StandardParagraph instances
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

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Object next() {
        if (next == null) throw new NoSuchElementException("No more elements to iterate");

        var result = next;
        next = null;
        if (result instanceof ContentAccessor contentAccessor) {
            var content = contentAccessor.getContent();
            iteratorQueue.add(content.iterator());
        }
        else if (result instanceof SdtRun sdtRun) {
            var content = sdtRun.getSdtContent()
                                .getContent();
            iteratorQueue.add(content.iterator());
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
        this.next = startingIterator.hasNext() ? startingIterator.next() : null;
    }
}
