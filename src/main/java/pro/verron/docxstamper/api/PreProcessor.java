package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * An interface for pre-processors that are called before the actual processing
 * of a document takes place.
 */
public interface PreProcessor {
    /**
     * Processes the given document before the actual processing takes place.
     *
     * @param document the document to process.
     */
    void process(WordprocessingMLPackage document);
}
