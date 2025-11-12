package pro.verron.officestamper.api;

import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;

import java.util.List;
import java.util.stream.Stream;

/// Represents a part of a WordprocessingML-based document. This interface extends the
/// DocxDocument interface and provides additional methods to retrieve specific parts,
/// manipulate document content, and stream elements such as paragraphs and runs.
public interface DocxPart
        extends DocxDocument {
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

    /// Retrieves the content of the WordprocessingML-based document as a list of objects.
    /// The content may include various document elements such as paragraphs, tables, runs, etc.
    ///
    /// @return a list of objects representing the document's content
    List<Object> content();

    /// Streams all paragraphs contained within the WordprocessingML-based document part.
    ///
    /// @return a stream of [Paragraph] objects representing the paragraphs in the document part
    @Deprecated Stream<Paragraph> streamParagraphs();

    /// Streams all run elements contained within the WordprocessingML-based document part.
    ///
    /// @return a stream of [R] objects representing the run elements in the document part
    @Deprecated Stream<R> streamRun();
}
