package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.utils.wml.DocxDocument;

/// The PostProcessor interface defines a contract for performing transformations
/// or manipulations on instances of [WordprocessingMLPackage].
/// Implementations of this interface are intended to encapsulate various
/// document processing operations, such as cleaning up document structure,
/// modifying content, or removing unused components.
///
/// A typical use of this interface might involve implementing custom logic
/// to traverse and manipulate the contents of a WordprocessingMLPackage
/// document, such as removing orphaned footnotes or endnotes.
public interface PostProcessor {
    /// Processes a given WordprocessingMLPackage document.
    /// This method is typically used for performing operations such as modifying
    /// content, cleaning up the document structure, or applying other transformations.
    ///
    /// @param document the [DocxDocument] document to process
    void process(DocxDocument document);
}
