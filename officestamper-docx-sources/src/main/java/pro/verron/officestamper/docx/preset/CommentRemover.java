package pro.verron.officestamper.docx.preset;

import pro.verron.officestamper.api.OfficeStamperException;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.CommentFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.PostProcessor;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;

/// The CommentRemover class is a concrete implementation of the PostProcessor interface.
/// This class is responsible for removing all comments and their corresponding elements
/// from a WordprocessingMLPackage pkg. It traverses the pkg, identifies comment
/// elements, removes them from their parent contents, and clears the comments stored in
/// the pkg's comments part.
///
/// Key responsibilities of this class include:
/// 1. Identifying all comment-related elements within the pkg using a CommentFinder instance.
/// 2. Removing those elements from their parent's content list.
/// 3. Accessing and clearing the pkg's comments part to ensure no residual comments remain.
///
/// This class is designed for scenarios where comments in a pkg need to be fully removed
/// as part of preprocessing or cleanup operations.
public class CommentRemover
        implements PostProcessor {
    /// Build a [CommentRemover] instance
    public CommentRemover() {}

    @Override
    public void process(OfficeDocument document) {
        var pkg = document.unwrap(WordprocessingMLPackage.class);
        var visitor = new CommentFinder();
        TraversalUtil.visit(pkg.getMainDocumentPart(), visitor);

        // Replaces tags with their content in parent
        for (var commentElement : visitor.getCommentElements()) {
            var parent = (ContentAccessor) commentElement.getParent();
            var siblings = parent.getContent();
            siblings.remove(commentElement);
        }

        var mainDocumentPart = pkg.getMainDocumentPart();
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





