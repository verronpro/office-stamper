package pro.verron.officestamper.api;

import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import pro.verron.officestamper.utils.DocumentScanner;

import java.util.List;
import java.util.stream.Stream;

public interface DocxPart
        extends DocxDocument {
    Part part();
    DocxPart from(ContentAccessor accessor);
    List<Object> content();
    Stream<Paragraph> streamParagraphs();
    Stream<R> streamRun();

    DocumentScanner scanner();
}
