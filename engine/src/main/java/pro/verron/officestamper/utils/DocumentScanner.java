package pro.verron.officestamper.utils;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.core.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    private final AtomicReference<Object> current = new AtomicReference<>();
    private final MainDocumentPart mainDocumentPart;
    private final WordprocessingMLPackage document;

    /// Constructs a new instance of the DocumentScanner class, initializing it with the specified
    /// WordprocessingMLPackage document.
    /// This includes collecting the main document part, footnotes and endnotes parts from the provided document to set
    /// up scanning.
    ///
    /// @param document the WordprocessingMLPackage document containing the main document part, footnotes part, and
    ///
    ///                                                 endnotes part
    public DocumentScanner(WordprocessingMLPackage document) {
        this.document = document;
        mainDocumentPart = this.document.getMainDocumentPart();
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
        var next = currentRoot.get(index);
        if (indexHolder.get() < currentRoot.size()) {
            roots.add(currentRoot);
            queue.add(indexHolder);
        }
        if (next instanceof JAXBElement<?> jaxbElement) next = jaxbElement.getValue();
        if (next instanceof ContentAccessor contentAccessor) {
            var content = contentAccessor.getContent();
            if (!content.isEmpty()) {
                roots.add(content);
                queue.add(new AtomicInteger(0));
            }
        }
        this.current.set(next);
        return next;
    }

    /// Processes the current iteration of the scanner using the specified comment processors,
    /// and an associated expression context while maintaining the iteration index.
    ///
    /// @param commentProcessors the `CommentProcessors` object containing processor mappings for handling specific
    ///
    ///
    ///                                                                            comment-related operations.
    /// @param expressionContext the expression context of type `T` used during the processing of comments.
    public <T> void process(
            CommentProcessors commentProcessors,
            ExpressionResolver expressionResolver,
            T expressionContext
    ) {
        if (current.get() instanceof Text text) {
            var value = text.getValue();
            if (value.contains("${") && value.substring(value.indexOf("${"))
                                             .contains("}")) {
                var from = StandardParagraph.from(new TextualDocxPart(document),
                        (P) ((Child) text.getParent()).getParent());
                var matcher = new Matcher("[#$]\\{", "\\}");
                var placeholder = new StandardPlaceholder(matcher,
                        value.substring(value.indexOf("${") + 2, value.indexOf("}") - 1));
                var context = from.processorContext(placeholder);
                commentProcessors.setContext(context);
                expressionResolver.setContext(expressionContext);
                var resolve = expressionResolver.resolve(placeholder);


            }
        }
        // Apply the given context of processing to the current iteration of the scanner, while keeping the iteration
        // index coherent.
    }
}
