package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.utils.wml.DocxDocument;
import pro.verron.officestamper.utils.wml.DocxDocument.Part;
import pro.verron.officestamper.utils.wml.Parent;

import java.util.List;

/// Represents a textual part of a DOCX document, encapsulating the content and structure of the part while enabling
/// various operations such as accessing paragraphs, runs, and related parts. It manages the association with the XML structure of a DOCX document.
public final class TextualDocxPart {
    private final DocxDocument document;
    private final Part part;
    private final Parent parent;

    /// Constructs a [TextualDocxPart] using the specified [DocxDocument], [Part], and [Parent].
    ///
    /// @param document the [WordprocessingMLPackage] representing the document to be processed.
    /// @param part     the specific part of the document being processed.
    /// @param parent   the content accessor associated with the document part.
    public TextualDocxPart(DocxDocument document, Part part, Parent parent) {
        this.document = document;
        this.part = part;
        this.parent = parent;
    }

    /// Retrieves the part associated with this instance of the document part.
    ///
    /// @return the [Part] object representing the specific part associated with this instance.
    public Part part() {
        return part;
    }

    /// Retrieves the list of content objects associated with this document part.
    ///
    /// @return a list of objects representing the content of the document part.
    public List<Object> content() {
        return parent.getContent();
    }

    /// Returns the [WordprocessingMLPackage] instance representing the document associated with this part.
    ///
    /// @return the [WordprocessingMLPackage] instance representing the document.
    public DocxDocument document() {
        return document;
    }
}
