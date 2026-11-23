package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.OfficeStamper;

/// Utility class providing factory methods for building docx4j-free stampers.
///
/// @author Joseph Verron
/// @since 1.9.0
public class OfficeStampers {

    private OfficeStampers() {
        throw new OfficeStamperException("OfficeStampers cannot be instantiated");
    }

    /// Creates a stamper for [OfficeDocument] sources from the given configuration.
    ///
    /// @param configuration the configuration to use for this stamper
    /// @return a stamper operating on [OfficeDocument] objects
    public static OfficeStamper<OfficeDocument> docxStamper(OfficeStamperConfiguration configuration) {
        return new DocxStamper(configuration);
    }
}
