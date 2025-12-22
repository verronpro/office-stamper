package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.api.DocxPart;

import java.util.List;

/// Represents a textual part of a DOCX document, encapsulating the content and structure of the part while enabling
/// various operations such as accessing paragraphs, runs, and related parts. This class functions as a concrete
/// implementation of the [DocxPart] interface. It manages the association with the XML structure of a DOCX document.
public final class TextualDocxPart
        implements DocxPart {
    private final WordprocessingMLPackage document;
    private final Part part;
    private final ContentAccessor contentAccessor;

    /// Constructs a [TextualDocxPart] using the provided `document`. This constructor initializes the instance with the
    /// main document part and content accessor derived from the provided `WordprocessingMLPackage`.
    ///
    /// @param document the [WordprocessingMLPackage] representing the document to be processed.
    public TextualDocxPart(WordprocessingMLPackage document) {
        this(document, document.getMainDocumentPart(), document.getMainDocumentPart());
    }

    /// Constructs a [TextualDocxPart] using the specified `document`, `part`, and `contentAccessor`.
    ///
    /// @param document the [WordprocessingMLPackage] representing the document to be processed.
    /// @param part the specific part of the document being processed.
    /// @param contentAccessor the content accessor associated with the document part.
    public TextualDocxPart(WordprocessingMLPackage document, Part part, ContentAccessor contentAccessor) {
        this.document = document;
        this.part = part;
        this.contentAccessor = contentAccessor;
    }

    /// Retrieves the part associated with this instance of the document part.
    ///
    /// @return the [Part] object representing the specific part associated with this instance.
    @Override
    public Part part() {return part;}

    /// Retrieves the list of content objects associated with this document part.
    ///
    /// @return a list of objects representing the content of the document part.
    @Override
    public List<Object> content() {return contentAccessor.getContent();}

    /// Returns the [WordprocessingMLPackage] instance representing the document associated with this part.
    ///
    /// @return the [WordprocessingMLPackage] instance representing the document.
    public WordprocessingMLPackage document() {return document;}
}
