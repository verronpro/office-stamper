package pro.verron.officestamper.api;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.CommentFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.jvnet.jaxb2_commons.ppp.Child;

public class CommentRemover
        implements PostProcessor {
    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new CommentFinder();
        TraversalUtil.visit(document.getMainDocumentPart(), visitor);

        // Replaces tags with their content in parent
        for (Child commentElement : visitor.getCommentElements()) {
            var parent = (ContentAccessor) commentElement.getParent();
            var siblings = parent.getContent();
            siblings.remove(commentElement);
        }

        var mainDocumentPart = document.getMainDocumentPart();
        if (mainDocumentPart == null) return;

        var commentsPart = mainDocumentPart.getCommentsPart();
        if (commentsPart == null) return;

        Comments commentsPartContents;
        try {
            commentsPartContents = commentsPart.getContents();
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }

        if (commentsPartContents == null) return;
        var comments = commentsPartContents.getComment();

        if (comments == null) return;
        comments.clear();
    }
}
