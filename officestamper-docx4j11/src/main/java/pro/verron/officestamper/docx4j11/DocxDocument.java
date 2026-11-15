package pro.verron.officestamper.docx4j11;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.OfficePart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of {@link OfficeDocument}.
public final class DocxDocument implements OfficeDocument {

    private final WordprocessingMLPackage pkg;

    DocxDocument(WordprocessingMLPackage pkg) {
        this.pkg = pkg;
    }

    /// Loads a document from the given stream.
    public static DocxDocument load(InputStream in) throws IOException {
        try {
            return new DocxDocument(WordprocessingMLPackage.load(in));
        } catch (Exception e) {
            throw new IOException("Failed to load DOCX", e);
        }
    }

    WordprocessingMLPackage pkg() {
        return pkg;
    }

    @Override
    public OfficePart mainPart() {
        return new DocxPart(pkg.getMainDocumentPart(), this);
    }

    @Override
    public List<OfficePart> parts() {
        var result = new ArrayList<OfficePart>();
        for (var part : pkg.getParts().getParts().values()) {
            if (part instanceof JaxbXmlPart jaxb) {
                result.add(new DocxPart(jaxb, this));
            }
        }
        return List.copyOf(result);
    }

    @Override
    public void save(OutputStream out) throws IOException {
        try {
            pkg.save(out);
        } catch (Exception e) {
            throw new IOException("Failed to save DOCX", e);
        }
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(pkg)) return type.cast(pkg);
        throw new IllegalArgumentException("Cannot unwrap OfficeDocument as " + type.getName());
    }
}
