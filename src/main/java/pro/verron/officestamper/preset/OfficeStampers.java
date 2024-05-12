package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.preprocessor.MergeSameStyleRuns;
import org.wickedsource.docxstamper.preprocessor.RemoveProofErrors;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperConfiguration;

/**
 * Main class of the docx-stamper library.
 * <p>
 * This class can be used to create "stampers" that will open .docx templates
 * to create a .docx document filled with custom data at runtime.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.4
 */
public class OfficeStampers {

	/**
	 * Creates a new instance of the {@link DocxStamper} class with the specified {@link OfficeStamperConfiguration}.
	 *
	 * @param config the configuration for the docx stamper
	 *
	 * @return a new instance of the {@link DocxStamper} class
	 */
	public static OfficeStamper<WordprocessingMLPackage> docxStamper(
			OfficeStamperConfiguration config
	) {
		return new DocxStamper<>(config);
	}

	/**
	 * Creates a new DocxStamper with the default configuration.
	 * Also adds the {@link RemoveProofErrors} and {@link MergeSameStyleRuns} preprocessors.
	 *
	 * @return a new DocxStamper
	 */
	public static OfficeStamper<WordprocessingMLPackage> docxStamper() {
		return new DocxStamper<>(OfficeStamperConfigurations.standardWithPreprocessing());
	}

}
