package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Represents a part of a WordprocessingML-based document. This interface extends the DocxDocument interface and
/// provides additional methods to retrieve specific parts, manipulate document content, and stream elements such as
/// paragraphs and runs.
public interface DocxPart {

    Optional<Comment> comment(BigInteger id);

    /// Retrieves the part of the WordprocessingML-based document.
    ///
    /// @return the part of the document
    Part part();

    /// Creates and returns a DocxPart instance from the provided ContentAccessor.
    ///
    /// @param accessor the ContentAccessor from which the DocxPart is created
    ///
    /// @return a DocxPart instance created from the given ContentAccessor
    DocxPart from(ContentAccessor accessor);

    /// Retrieves the content of the WordprocessingML-based document as a list of objects. The content may include
    /// various document elements such as paragraphs, tables, runs, etc.
    ///
    /// @return a list of objects representing the document's content
    List<Object> content();

    String type();

    Map<BigInteger, Comment> comments();

    WordprocessingMLPackage document();
}
