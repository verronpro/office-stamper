package pro.verron.officestamper.experimental;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.utils.DocumentScanner;

import java.util.List;
import java.util.stream.Stream;

public class PptxPart
        implements DocxPart {
    @Override public Part part() {
        return null;
    }

    @Override public DocxPart from(ContentAccessor accessor) {
        return null;
    }

    @Override public List<Object> content() {
        return List.of();
    }

    @Override public Stream<Paragraph> streamParagraphs() {
        return Stream.empty();
    }

    @Override public Stream<R> streamRun() {
        return Stream.empty();
    }

    @Override
    public DocumentScanner scanner() {
        return null;
    }

    @Override public WordprocessingMLPackage document() {
        return null;
    }

    @Override public Stream<DocxPart> streamParts(String type) {
        return Stream.empty();
    }
}
