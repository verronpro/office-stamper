package pro.verron.officestamper.utils;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.vml.CTTextbox;
import org.docx4j.vml.VmlShapeElements;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Pict;
import org.docx4j.wml.SdtElement;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    /// Constructs a new instance of the DocumentScanner class, initializing it with the specified
    /// WordprocessingMLPackage document.
    /// This includes collecting the main document part, footnotes and endnotes parts from the provided document to set
    /// up scanning.
    ///
    public DocumentScanner(Part part) {
        var mainRoot = new ArrayList<>();
        mainRoot.add(part);
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
        if (next instanceof JaxbXmlPart part) {
            try {
                next = part.getContents();
            } catch (Docx4JException e) {
                throw new OfficeStamperException(e);
            }
        }
        if (next instanceof JAXBElement<?> jaxbElement) next = jaxbElement.getValue();
        if (next instanceof Pict pict) next = pict.getAnyAndAny();
        if (next instanceof CTTextbox ctTextbox) next = ctTextbox.getTxbxContent();
        if (next instanceof VmlShapeElements vmlShapeElements) next = vmlShapeElements.getEGShapeElements();
        if (next instanceof SdtElement sdtElement) next = sdtElement.getSdtContent();
        if (next instanceof List list) {
            if (!list.isEmpty()) {
                roots.add(list);
                queue.add(new AtomicInteger(0));
            }
        }
        else if (next instanceof ContentAccessor contentAccessor) {
            var content = contentAccessor.getContent();
            if (!content.isEmpty()) {
                roots.add(content);
                queue.add(new AtomicInteger(0));
            }
        }
        this.current.set(next);
        return next;
    }
}
