package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/// The PreProcessor interface defines a method for processing a WordprocessingMLPackage
/// document prior to executing specific operations.
/// Implementations of this interface can modify, sanitize, or transform the document content as necessary.
public interface PreProcessor {
    /// Processes the provided WordprocessingMLPackage document based on implementation-specific behavior.
    /// This method allows for manipulation or transformation of the document, such as modifying its content,
    /// sanitizing specific sections, or preparing the document for further actions.
    ///
    /// @param document the WordprocessingMLPackage document to be processed; cannot be null
    void process(WordprocessingMLPackage document);
}
