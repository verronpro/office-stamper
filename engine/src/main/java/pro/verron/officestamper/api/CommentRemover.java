package pro.verron.officestamper.api;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.CommentFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.jvnet.jaxb2_commons.ppp.Child;

/// The CommentRemover class is a concrete implementation of the PostProcessor interface.
/// This class is responsible for removing all comments and their corresponding elements
/// from a WordprocessingMLPackage document. It traverses the document, identifies comment
/// elements, removes them from their parent contents, and clears the comments stored in
/// the document's comments part.
///
/// Key responsibilities of this class include:
/// 1. Identifying all comment-related elements within the document using a CommentFinder instance.
/// 2. Removing those elements from their parent's content list.
/// 3. Accessing and clearing the document's comments part to ensure no residual comments remain.
///
/// This class is designed for scenarios where comments in a document need to be fully removed
/// as part of preprocessing or cleanup operations.
public class CommentRemover
        implements PostProcessor {
    /// Build a [CommentRemover] instance
    public CommentRemover() {}

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
