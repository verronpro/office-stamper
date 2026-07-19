package pro.verron.officestamper.utils.wml;

import org.docx4j.model.structure.DocumentModel;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.docx4j.openpackaging.parts.relationships.Namespaces.FOOTER;
import static org.docx4j.openpackaging.parts.relationships.Namespaces.HEADER;

public class DocxDocument implements Document {
    private final WordprocessingMLPackage mlPackage;

    public DocxDocument(WordprocessingMLPackage mlPackage) {
        this.mlPackage = mlPackage;
    }

    /// Loads a DocxDocument document from the provided input stream.
    ///
    /// @param is the input stream containing the DocxDocument document data
    /// @return a [DocxDocument] representing the loaded document
    public static DocxDocument load(InputStream is) {
        var mlPackage = OpenpackagingUtils.loadWord(is);
        return new DocxDocument(mlPackage);
    }

    /// Exports a DocxDocument document to the provided output stream.
    ///
    /// @param outputStream the output stream to write the document to
    /// @throws UtilsException if there is an error exporting the document
    @Override
    public void save(OutputStream outputStream) {
        try {
            mlPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }

    public MainDocumentPart getMainDocumentPart() {
        return mlPackage.getMainDocumentPart();
    }

    public DocumentModel getDocumentModel() {
        return mlPackage.getDocumentModel();
    }

    public WordprocessingMLPackage getPackage() {
        return mlPackage;
    }

    public Part mainPart() {
        return new Part(this, mlPackage.getMainDocumentPart(), mlPackage.getMainDocumentPart()::getContent);
    }

    public List<DocxDocument.Part> headerParts() {
        return partsByType(HEADER);
    }

    public List<DocxDocument.Part> footerParts() {
        return partsByType(FOOTER);
    }

    private List<Part> partsByType(String type) {
        var mainDocumentPart = mlPackage.getMainDocumentPart();
        var relationshipsPart = mainDocumentPart.getRelationshipsPart();
        return relationshipsPart.getRelationshipsByType(type)
                .stream()
                .map(relationshipsPart::getPart)
                .map(part -> new Part(this, part, ((ContentAccessor) part)::getContent))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static class Comment {
        private final Part part;
        private final CTSmartTagRun parent;
        private final CommentRangeStart crs;
        private final CommentRangeEnd cre;
        private final Comments.Comment comment;
        private final R.CommentReference cr;

        public Comment(Part part, CTSmartTagRun parent, CommentRangeStart crs, CommentRangeEnd cre, Comments.Comment comment, R.CommentReference cr) {
            this.part = part;
            this.parent = parent;
            this.crs = crs;
            this.cre = cre;
            this.comment = comment;
            this.cr = cr;
        }

        public CommentRangeEnd getCommentRangeEnd() {
            return cre;
        }

        public CommentRangeStart getCommentRangeStart() {
            return crs;
        }

        public R.CommentReference getCommentReference() {
            return cr;
        }

        public Comments.Comment getWmlComment() {
            return comment;
        }

        public CTSmartTagRun getTagRun() {
            return parent;
        }

        public Part getPart() {
            return part;
        }
    }

    public static class Part {
        private final DocxDocument document;
        private final org.docx4j.openpackaging.parts.Part part;
        private final Parent content;

        public Part(DocxDocument document, org.docx4j.openpackaging.parts.Part part, Parent content) {
            this.document = document;
            this.part = part;
            this.content = content;
        }

        public DocxDocument document() {
            return document;
        }

        public org.docx4j.openpackaging.parts.Part getPart() {
            return part;
        }

        public List<Object> content() {
            return content.getContent();
        }
    }
}
