package pro.verron.officestamper.docx4j11;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.utils.wml.DocxIterator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// Comments-part access for the docx4j 11 provider (port of the engine's comment lookup logic).
final class DocxComments {

    private static final PartName COMMENTS_PART_NAME;

    static {
        try {
            COMMENTS_PART_NAME = new PartName("/word/comments.xml");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private DocxComments() {
    }

    static List<Comments.Comment> all(OpcPackage pkg) {
        return commentsPart(pkg.getParts())
                .map(DocxComments::extract)
                .map(Comments::getComment)
                .stream()
                .flatMap(List::stream)
                .toList();
    }

    /// Finds the comments anchored in the given content (port of the legacy `CommentUtil.getCommentFor`).
    static List<Comments.Comment> findFor(ContentAccessor content, OpcPackage pkg) {
        var all = all(pkg);
        var result = new ArrayList<Comments.Comment>();
        var iterator = new DocxIterator(content).selectClass(CommentRangeStart.class);
        while (iterator.hasNext()) {
            var crs = iterator.next();
            find(all, crs.getId()).ifPresent(result::add);
        }
        return result;
    }

    /// Looks up a comment object by its id within the document's comments part.
    static Comments.Comment byId(OpcPackage pkg, BigInteger id) {
        return find(all(pkg), id).orElse(null);
    }

    private static Optional<Comments.Comment> find(List<Comments.Comment> comments, BigInteger id) {
        for (var comment : comments) {
            if (Objects.equals(id, comment.getId())) return Optional.of(comment);
        }
        return Optional.empty();
    }

    private static Optional<CommentsPart> commentsPart(Parts parts) {
        return Optional.ofNullable((CommentsPart) parts.get(COMMENTS_PART_NAME));
    }

    private static Comments extract(CommentsPart part) {
        try {
            return part.getContents();
        } catch (Exception e) {
            throw new IllegalStateException("Error while searching comment.", e);
        }
    }
}
