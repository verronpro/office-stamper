package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.DocxPart;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class TextualDocxPart
        implements DocxPart {
    private final WordprocessingMLPackage document;
    private final Part part;
    private final ContentAccessor contentAccessor;

    public TextualDocxPart(WordprocessingMLPackage document) {
        this(document, document.getMainDocumentPart(), document.getMainDocumentPart());
    }

    public TextualDocxPart(
            WordprocessingMLPackage document,
            Part part,
            ContentAccessor contentAccessor
    ) {
        this.document = document;
        this.part = part;
        this.contentAccessor = contentAccessor;
    }

    public Stream<P> streamParagraphs() {
        return DocumentUtil.streamObjectElements(this, P.class);
    }

    public Stream<DocxPart> streamParts(String type) {
        return document.getMainDocumentPart()
                       .getRelationshipsPart()
                       .getRelationshipsByType(type)
                       .stream()
                       .map(this::getPart)
                       .map(p -> new TextualDocxPart(document, p, (ContentAccessor) p));
    }

    public Part getPart(Relationship r) {
        return getRelationshipsPart().getPart(r);
    }

    public WordprocessingMLPackage document() {return document;}

    private RelationshipsPart getRelationshipsPart() {
        return part().getRelationshipsPart();
    }

    public CommentsPart commentsPart() {
        return CommentUtil.getCommentsPart(document.getParts());
    }

    @Override public DocxPart from(ContentAccessor accessor) {
        return new TextualDocxPart(document, part, accessor);
    }

    @Override public Part part() {return part;}

    @Override public List<Object> content() {return contentAccessor.getContent();}

    @Override
    public int hashCode() {
        return Objects.hash(document, part, contentAccessor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextualDocxPart) obj;
        return Objects.equals(this.document, that.document) &&
               Objects.equals(this.part, that.part) &&
               Objects.equals(this.contentAccessor, that.contentAccessor);
    }

    @Override public String toString() {
        return "DocxPart{doc=%s, part=%s}".formatted(document.name(), part.getPartName());
    }

}
