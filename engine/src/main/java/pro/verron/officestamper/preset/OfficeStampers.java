package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.StreamStamper;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils;

/// [OfficeStampers] is a utility class that provides factory methods for creating document stampers for Office
/// documents. This class offers convenient methods to create stampers for DOCX documents with various configurations.
///
/// The stampers created by this utility can apply various preprocessing steps to enhance the document processing
/// capabilities.
public class OfficeStampers {

    private OfficeStampers() {
        throw new OfficeStamperException("Utility classes should not be instantiated");
    }

    /// Creates a new instance of a [StreamStamper] for handling [WordprocessingMLPackage] documents with a default full
    /// configuration.
    ///
    /// @return a [StreamStamper] instance for stamping [WordprocessingMLPackage] documents
    ///
    /// @see OfficeStamperConfigurations#full()
    public static StreamStamper<WordprocessingMLPackage> docxStamper() {
        return docxStamper(OfficeStamperConfigurations.full());
    }

    /// Creates a [StreamStamper] instance that processes [WordprocessingMLPackage] (DOCX) documents by applying
    /// stamping with the given configuration.
    ///
    /// The returned stamper is designed to handle the transformation of DOCX templates using provided context data.
    ///
    /// @param configuration an instance of [OfficeStamperConfiguration] that defines the behavior and
    ///         preprocessing steps of the stamper
    ///
    /// @return a [StreamStamper] of [WordprocessingMLPackage] configured to process DOCX documents
    public static StreamStamper<WordprocessingMLPackage> docxStamper(OfficeStamperConfiguration configuration) {
        var stamper = docxPackageStamper(configuration);
        return new StreamStamper<>(OpenpackagingUtils::loadWord, stamper, OpenpackagingUtils::exportWord);
    }

    /// Creates an [OfficeStamper] instance for processing [WordprocessingMLPackage] documents with the specified
    /// configuration.
    ///
    /// @param configuration an instance of [OfficeStamperConfiguration] that defines the behavior of the
    ///         stamper
    ///
    /// @return an [OfficeStamper] for [WordprocessingMLPackage] configured to process DOCX documents
    public static OfficeStamper<WordprocessingMLPackage> docxPackageStamper(OfficeStamperConfiguration configuration) {
        return new DocxStamper(configuration);
    }
}
