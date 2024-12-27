package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.StreamStamper;
import pro.verron.officestamper.core.DocxStamper;

import java.io.InputStream;

/// Main class of the docx-stamper library.
///
/// This class can be used to create "stampers" that will open .docx templates
/// to create a .docx document filled with custom data at runtime.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public class OfficeStampers {


    private OfficeStampers() {
        throw new OfficeStamperException("OfficeStampers cannot be instantiated");
    }

    /// Creates a new DocxStamper with the default configuration.
    /// Also adds the [#removeLanguageProof()] and [#mergeSimilarRuns()]
    /// preprocessors.
    ///
    /// @return a new DocxStamper
    public static StreamStamper<WordprocessingMLPackage> docxStamper() {
        return docxStamper(OfficeStamperConfigurations.standard());
    }

    /// Creates a new instance of the [DocxStamper] class with the specified [OfficeStamperConfiguration].
    ///
    /// @param config the configuration for the docx stamper
    ///
    /// @return a new instance of the [DocxStamper] class
    public static StreamStamper<WordprocessingMLPackage> docxStamper(
            OfficeStamperConfiguration config
    ) {
        return new StreamStamper<>(
                OfficeStampers::loadWord,
                new DocxStamper(config)
        );
    }

    private static WordprocessingMLPackage loadWord(InputStream is) {
        try {
            return WordprocessingMLPackage.load(is);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

}
