package pro.verron.officestamper.docx4j11.experimental;

import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.OfficePart;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of [OfficeDocument] for PowerPoint presentations,
/// used by the experimental pptx stampers.
public final class PptxDocument implements OfficeDocument {

    private final PresentationMLPackage pkg;

    public PptxDocument(PresentationMLPackage pkg) {
        this.pkg = pkg;
    }

    @Override
    public OfficePart mainPart() {
        return new NativePart(pkg.getMainPresentationPart(), this);
    }

    @Override
    public List<OfficePart> parts() {
        var result = new ArrayList<OfficePart>();
        for (var part : pkg.getParts().getParts().values()) {
            if (part instanceof JaxbXmlPart jaxb && jaxb instanceof org.docx4j.wml.ContentAccessor) {
                result.add(new NativePart(jaxb, this));
            }
        }
        return List.copyOf(result);
    }

    @Override
    public void save(OutputStream out) throws IOException {
        try {
            pkg.save(out);
        } catch (Exception e) {
            throw new IOException("Failed to save PPTX", e);
        }
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(pkg)) return type.cast(pkg);
        throw new IllegalArgumentException("Cannot unwrap OfficeDocument as " + type.getName());
    }
}
