package org.wickedsource.docxstamper.api.preprocessor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * The interface for all pre-processors. Pre-processors are called before the
 * document is processed by the DocxStamper. They can be used to manipulate the
 * document before the actual processing takes place.
 *
 * @see org.wickedsource.docxstamper.DocxStamper
 * @see org.wickedsource.docxstamper.preprocessor.MergeSameStyleRuns
 * @see org.wickedsource.docxstamper.preprocessor.RemoveProofErrors
 * @author Joseph Verron
 * @version 1.6.6
 */
public interface PreProcessor {
	/**
	 * Processes the given document before the actual processing takes place.
	 *
	 * @param document the document to process.
	 */
	void process(WordprocessingMLPackage document);
}
