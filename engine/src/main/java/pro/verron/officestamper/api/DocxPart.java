package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.DocumentUtil;

import java.util.stream.Stream;

public record DocxPart(
        WordprocessingMLPackage document,
        Part part,
        ContentAccessor contentAccessor
) {

    public DocxPart(WordprocessingMLPackage document) {
        this(document, document.getMainDocumentPart(), document.getMainDocumentPart());
    }

    public Stream<P> streamParagraphs() {
        return DocumentUtil.streamObjectElements(this, P.class);
    }

    public Stream<DocxPart> getParts(String namespace) {
        return document.getMainDocumentPart()
                       .getRelationshipsPart()
                       .getRelationshipsByType(namespace)
                       .stream()
                       .map(this::getPart)
                       .map(part -> new DocxPart(this.document(), part, (ContentAccessor) part));
    }

    public Part getPart(Relationship r) {
        return getRelationshipsPart().getPart(r);
    }

    private RelationshipsPart getRelationshipsPart() {
        return part().getRelationshipsPart();
    }

    public CommentsPart getCommentsPart() {
        var parts = document.getParts();
        return (CommentsPart) parts.get(CommentUtil.WORD_COMMENTS_PART_NAME);
    }

    @Override public String toString() {
        return "DocxPart{doc=%s, part=%s}".formatted(document.name(), part.getPartName());
    }
}
