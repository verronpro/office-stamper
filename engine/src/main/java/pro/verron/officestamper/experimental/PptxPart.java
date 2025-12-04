package pro.verron.officestamper.experimental;

import org.docx4j.openpackaging.packages.PresentationMLPackage;

/// The PptxPart class represents a specific implementation of the DocxPart interface designed for handling parts within
/// a PowerPoint document.
public record PptxPart(PresentationMLPackage document) {

    /// Constructs a new instance of the PptxPart class.
    ///
    /// This constructor initializes an instance of PptxPart, which represents a specific implementation of the DocxPart
    /// interface tailored for handling parts within a PowerPoint document. This class provides methods to interact with
    /// and manipulate the content and structure of parts in a PowerPoint file.
    public PptxPart {
    }
}
