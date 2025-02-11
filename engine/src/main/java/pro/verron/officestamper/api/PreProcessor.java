package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/// An interface for pre-processors called before the actual processing
/// of a document takes place.
public interface PreProcessor {

    /// Processes the given document before the actual processing takes place.
    ///
    /// @param document the document to process.
    void process(WordprocessingMLPackage document);
}
