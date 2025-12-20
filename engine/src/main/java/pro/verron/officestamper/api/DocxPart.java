package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;

import java.util.List;

/// Represents a part of a WordprocessingML-based document. This interface extends the DocxDocument interface and
/// provides additional methods to retrieve specific parts, manipulate document content, and stream elements such as
/// paragraphs and runs.
public interface DocxPart {

    /// Retrieves the part of the WordprocessingML-based document.
    ///
    /// @return the part of the document
    Part part();

    /// Retrieves the content of the WordprocessingML-based document as a list of objects. The content may include
    /// various document elements such as paragraphs, tables, runs, etc.
    ///
    /// @return a list of objects representing the document's content
    List<Object> content();

    /// Retrieves the [WordprocessingMLPackage] representing the entire document.
    ///
    /// @return the [WordprocessingMLPackage] of the document
    WordprocessingMLPackage document();
}
