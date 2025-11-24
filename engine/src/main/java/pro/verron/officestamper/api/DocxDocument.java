package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.util.List;

/// The DocxDocument interface represents a WordprocessingML-based document,
/// providing methods to access the document and stream its parts by type.
public interface DocxDocument {
    /// Retrieves the WordprocessingMLPackage document associated with this instance.
    /// The returned document provides access to its content and metadata.
    ///
    /// @return the WordprocessingMLPackage document
    WordprocessingMLPackage document();

    /// Streams the parts of the document that match the specified type.
    ///
    /// @param type the type of parts to stream, typically used to filter specific types of document parts
    ///
    /// @return a stream of `DocxPart` objects that match the specified type
    List<DocxPart> parts(String type);
}
