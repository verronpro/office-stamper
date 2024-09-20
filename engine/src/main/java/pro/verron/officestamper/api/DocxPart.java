package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;

import java.util.List;
import java.util.stream.Stream;

public interface DocxPart {
    DocxPart from(ContentAccessor accessor);

    Part part();

    List<Object> content();

    @Deprecated(since = "2.5")
    WordprocessingMLPackage document();

    @Deprecated(since = "2.5")
    CommentsPart commentsPart();

    @Deprecated(since = "2.5")
    Stream<P> streamParagraphs();

    @Deprecated(since = "2.5")
    Stream<DocxPart> streamParts(String header);
}
