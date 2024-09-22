package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;

import java.util.stream.Stream;

public interface DocxDocument {
    WordprocessingMLPackage document();
    Stream<DocxPart> streamParts(String type);
    CommentsPart commentsPart();
}
