package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.StreamStamper;
import pro.verron.officestamper.core.DocxStamper;

import java.io.InputStream;

/// [OfficeStampers] is a utility class designed to provide methods for manipulating and stamping Office documents. This
/// class includes factory methods for creating document stampers, specifically [StreamStamper] instances for handling
/// [WordprocessingMLPackage] documents.
///
/// Various preprocessors may be applied during the creation of these stampers to modify or enhance the operation of the
/// stamper.
public class OfficeStampers {

    private OfficeStampers() {
        throw new OfficeStamperException("Utility classes should not be instantiated");
    }

    /// Creates a new instance of a [StreamStamper] for handling [WordprocessingMLPackage] documents with a default
    /// configuration.
    ///
    /// @return a [StreamStamper] instance for stamping [WordprocessingMLPackage] documents
    public static StreamStamper<WordprocessingMLPackage> docxStamper() {
        return docxStamper(OfficeStamperConfigurations.full());
    }

    /// Creates a [StreamStamper] instance that processes [WordprocessingMLPackage] (DOCX) documents by applying
    /// stamping with the given configuration.
    ///
    /// The returned stamper is designed to handle the transformation of DOCX templates using provided context data.
    ///
    /// @param configuration an instance of [OfficeStamperConfiguration] that defines the behavior and
    ///         preprocessing steps of the stamper.
    ///
    /// @return a [StreamStamper] of type [WordprocessingMLPackage] configured to process DOCX documents.
    public static StreamStamper<WordprocessingMLPackage> docxStamper(OfficeStamperConfiguration configuration) {
        return new StreamStamper<>(OfficeStampers::loadWord, new DocxStamper(configuration));
    }

    private static WordprocessingMLPackage loadWord(InputStream is) {
        try {
            return WordprocessingMLPackage.load(is);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

}
