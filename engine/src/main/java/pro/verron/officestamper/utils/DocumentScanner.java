package pro.verron.officestamper.utils;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.core.CommentProcessors;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Optional.ofNullable;
import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// The DocumentScanner class provides an iterative mechanism for traversing through the contents
/// of a WordprocessingMLPackage document. It initializes with the document's main part as well as
/// optional footnotes and endnotes, enabling structured iteration over the elements of the document.
/// It supports the following operations:
/// - Iteration through the document's hierarchical contents using hasNext() and next() methods.
/// - Processing content during iteration using the process method, which integrates comment processors,
///   and an associated expression context.
/// This class is designed to handle JAXB elements and content accessors, dynamically traversing nested
/// structures within the document while maintaining internal state for iteration.
/// The primary use case of this class is to enable document manipulation or extraction scenarios
/// where structured access to the document's hierarchy is required.
public final class DocumentScanner {
    private final Queue<List<Object>> roots = Collections.asLifoQueue(new LinkedList<>());
    private final Queue<AtomicInteger> queue = Collections.asLifoQueue(new LinkedList<>());

    /// Constructs a new instance of the DocumentScanner class, initializing it with the specified
    /// WordprocessingMLPackage document.
    /// This includes collecting the main document part, footnotes and endnotes parts from the provided document to set
    /// up scanning.
    ///
    /// @param document the WordprocessingMLPackage document containing the main document part, footnotes part, and
    ///                 endnotes part
    public DocumentScanner(WordprocessingMLPackage document) {
        var mainDocumentPart = document.getMainDocumentPart();
        var footnotesPart = mainDocumentPart.getFootnotesPart();
        var endnotesPart = mainDocumentPart.getEndNotesPart();

        var mainRoot = new ArrayList<>();
        mainRoot.add(mainDocumentPart);
        ofNullable(footnotesPart).map(throwing(FootnotesPart::getContents))
                                 .ifPresent(mainRoot::add);
        ofNullable(endnotesPart).map(throwing(EndnotesPart::getContents))
                                .ifPresent(mainRoot::add);
        roots.add(mainRoot);
        queue.add(new AtomicInteger(0));
    }

    /// Checks if there are more elements to scan in the current document.
    ///
    /// @return `true` if there are more elements to scan; `false` otherwise.
    public boolean hasNext() {
        return !roots.isEmpty();
    }

    /// Retrieves the next object in the scanning process, processing the current root
    /// and its associated content. If the current root contains additional elements,
    /// it re-enqueues the root and updates the state accordingly. The method also handles
    /// the extraction of values from JAXBElement and further content navigation from
    /// ContentAccessor objects.
    ///
    /// @return the next object in the iteration sequence.
    public Object next() {
        var currentRoot = roots.remove();
        var indexHolder = queue.remove();
        var index = indexHolder.getAndIncrement();
        var current = currentRoot.get(index);
        if (indexHolder.get() < currentRoot.size()) {
            roots.add(currentRoot);
            queue.add(indexHolder);
        }
        if (current instanceof JAXBElement<?> jaxbElement) current = jaxbElement.getValue();
        if (current instanceof ContentAccessor contentAccessor) {
            var content = contentAccessor.getContent();
            if (!content.isEmpty()) {
                roots.add(content);
                queue.add(new AtomicInteger(0));
            }
        }

        return current;
    }

    /// Processes the current iteration of the scanner using the specified comment processors,
    /// and an associated expression context while maintaining the iteration index.
    ///
    /// @param commentProcessors the `CommentProcessors` object containing processor mappings for handling specific
    ///                          comment-related operations.
    /// @param expressionContext the expression context of type `T` used during the processing of comments.
    public <T> void process(CommentProcessors commentProcessors, T expressionContext) {
        //Apply the given context of processing to the current iteration of the scanner, while keeping the iteration
        // index coherent.
    }
}
