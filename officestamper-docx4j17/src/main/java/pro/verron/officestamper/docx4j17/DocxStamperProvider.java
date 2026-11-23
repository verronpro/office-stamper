package pro.verron.officestamper.docx4j17;

import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.OfficeStamper;

/// ServiceLoader provider for the docx4j 11 implementation of the OfficeStamper SPI.
///
/// Registered in `META-INF/services/pro.verron.officestamper.api.OfficeStamper` and in the module
/// descriptor; loaded by [pro.verron.officestamper.api.OfficeStampers#docxStamper].
///
/// @author Joseph Verron
/// @since 1.9.0
public class DocxStamperProvider
        implements OfficeStamper<OfficeDocument> {

    private final OfficeStamper<OfficeDocument> delegate =
            pro.verron.officestamper.preset.OfficeStampers.docxStamper(DocxOfficeStampers.full());

    @Override
    public OfficeDocument stamp(OfficeDocument document, Object contextRoot) {
        return delegate.stamp(document, contextRoot);
    }
}
