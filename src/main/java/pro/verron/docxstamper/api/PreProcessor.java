package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public interface PreProcessor {
    /**
     * Processes the given document before the actual processing takes place.
     *
     * @param document the document to process.
     */
    void process(WordprocessingMLPackage document);
}
