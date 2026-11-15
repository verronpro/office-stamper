package pro.verron.officestamper.docx4j11;

import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.OfficeStamper;

/// ServiceLoader provider for the docx4j 11 implementation of the OfficeStamper SPI.
///
/// Registered in `META-INF/services/pro.verron.officestamper.spi.OfficeStamper` and in the module
/// descriptor; loaded by [pro.verron.officestamper.spi.OfficeStampers#docxStamper].
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
