package org.wickedsource.docxstamper.api.preprocessor;

import org.wickedsource.docxstamper.preprocessor.MergeSameStyleRuns;
import org.wickedsource.docxstamper.preprocessor.RemoveProofErrors;

/**
 * The interface for all pre-processors. Pre-processors are called before the
 * document is processed by the DocxStamper. They can be used to manipulate the
 * document before the actual processing takes place.
 *
 * @see MergeSameStyleRuns
 * @see RemoveProofErrors
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.4
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the {@link pro.verron.officestamper.api.PreProcessor} class instead.
 * This class will not be exported in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public interface PreProcessor
		extends pro.verron.officestamper.api.PreProcessor {
}
