package pro.verron.officestamper.docx4j11;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.CommentFinder;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.Comments;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.spi.Comment;
import pro.verron.officestamper.spi.Content;
import pro.verron.officestamper.spi.ElementFactory;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.OfficePart;

import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of {@link OfficePart}.
public final class DocxPart implements OfficePart {

    private final JaxbXmlPart part;
    private final DocxDocument document;

    DocxPart(JaxbXmlPart part, DocxDocument document) {
        this.part = part;
        this.document = document;
    }

    @Override
    public String name() {
        return part.getPartName().getName();
    }

    @Override
    public Content content() {
        return new DocxContent(part, this);
    }

    @Override
    public List<Comment> comments() {
        var finder = new CommentFinder();
        try {
            new TraversalUtil(part.getContents(), finder);
        } catch (Exception e) {
            return List.of();
        }
        var result = new ArrayList<Comment>();
        for (Child c : finder.getCommentElements()) {
            if (c instanceof Comments.Comment cm) result.add(new DocxComment(cm));
        }
        return List.copyOf(result);
    }

    @Override
    public OfficeDocument document() {
        return document;
    }

    @Override
    public ElementFactory factory() {
        return new DocxElementFactory(this);
    }

    JaxbXmlPart part() {
        return part;
    }
}
