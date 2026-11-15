package pro.verron.officestamper.docx4j11;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.OfficeStamper;

import java.io.IOException;

/// docx4j 11 implementation of {@link OfficeStamper}.
///
/// <p>In this first phase the stamping logic is delegated to the existing engine
/// {@link DocxStamper}; the provider exists to decouple the public contract from
/// docx4j. In a later phase the stamping logic is moved into this module so the
/// engine no longer depends on docx4j.
public final class DocxStamper implements OfficeStamper<DocxDocument> {

    private final pro.verron.officestamper.core.DocxStamper delegate;

    public DocxStamper() {
        this.delegate = new pro.verron.officestamper.core.DocxStamper(OfficeStamperConfigurations.full());
    }

    public DocxStamper(OfficeStamperConfiguration configuration) {
        this.delegate = new pro.verron.officestamper.core.DocxStamper(configuration);
    }

    @Override
    public DocxDocument stamp(DocxDocument template, Object context) throws IOException {
        WordprocessingMLPackage result = delegate.stamp(template.pkg(), context);
        return new DocxDocument(result);
    }
}
