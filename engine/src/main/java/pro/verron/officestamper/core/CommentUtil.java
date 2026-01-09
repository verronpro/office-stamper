package pro.verron.officestamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.utils.wml.DocxIterator;

import java.math.BigInteger;
import java.util.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/// Utility class for working with comments in a DOCX document.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class CommentUtil {
    private static final PartName WORD_COMMENTS_PART_NAME;

    static {
        try {
            WORD_COMMENTS_PART_NAME = new PartName("/word/comments.xml");
        } catch (InvalidFormatException e) {
            throw new OfficeStamperException(e);
        }
    }

    private CommentUtil() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /// Retrieves the comment associated with a given paragraph content within a WordprocessingMLPackage document.
    ///
    /// @param contentAccessor the content accessor to search for comments.
    /// @param document        the WordprocessingMLPackage document containing the paragraph and its comments.
    /// @return a collection of found comments.
    public static Collection<Comments.Comment> getCommentFor(ContentAccessor contentAccessor, OpcPackage document) {
        var comments = getCommentsPart(document.getParts()).map(CommentUtil::extractContent)
                                                           .map(Comments::getComment)
                                                           .stream()
                                                           .flatMap(Collection::stream)
                                                           .toList();

        var result = new ArrayList<Comments.Comment>();
        var commentIterator = new DocxIterator(contentAccessor).selectClass(CommentRangeStart.class);
        while (commentIterator.hasNext()) {
            var crs = commentIterator.next();
            findCommentById(comments, crs.getId()).ifPresent(result::add);
        }
        return result;
    }

    /// Retrieves the CommentsPart from the given Parts object.
    ///
    /// @param parts the Parts object containing the various parts of the document.
    ///
    /// @return an Optional containing the CommentsPart if found, or an empty Optional if not found.
    public static Optional<CommentsPart> getCommentsPart(Parts parts) {
        return Optional.ofNullable((CommentsPart) parts.get(WORD_COMMENTS_PART_NAME));
    }

    /// Extracts the contents of a given [CommentsPart].
    ///
    /// @param commentsPart the [CommentsPart] from which content will be extracted
    ///
    /// @return the [Comments] instance containing the content of the provided comments part
    ///
    /// @throws OfficeStamperException if an error occurs while retrieving the content
    public static Comments extractContent(CommentsPart commentsPart) {
        try {
            return commentsPart.getContents();
        } catch (Docx4JException e) {
            throw new OfficeStamperException("Error while searching comment.", e);
        }
    }

    private static Optional<Comments.Comment> findCommentById(List<Comments.Comment> comments, BigInteger id) {
        for (Comments.Comment comment : comments) {
            if (id.equals(comment.getId())) {
                return Optional.of(comment);
            }
        }
        return Optional.empty();
    }

    /// Returns the string value of the specified comment object.
    ///
    /// @param comment a [Comment] object
    public static void deleteComment(Comment comment) {
        CommentRangeEnd end = comment.getCommentRangeEnd();
        ContentAccessor endParent = (ContentAccessor) end.getParent();
        endParent.getContent()
                 .remove(end);
        CommentRangeStart start = comment.getCommentRangeStart();
        var parent = start.getParent();
        ContentAccessor startParent = (ContentAccessor) parent;
        startParent.getContent()
                   .remove(start);
        if (startParent instanceof CTSmartTagRun tag && tag.getContent()
                                                           .isEmpty()) ((ContentAccessor) tag.getParent()).getContent()
                                                                                                          .remove(tag);
        CommentReference reference = comment.getCommentReference();
        if (reference != null) {
            ContentAccessor referenceParent = (ContentAccessor) reference.getParent();
            referenceParent.getContent()
                           .remove(reference);
        }
    }

    /// Creates a [Comment] object.
    ///
    /// @param docxPart the document part.
    /// @param crs the comment range start.
    /// @param document the document.
    /// @param contentAccessor the content accessor.
    ///
    /// @return the comment.
    public static Comment comment(
            DocxPart docxPart,
            CommentRangeStart crs,
            WordprocessingMLPackage document,
            ContentAccessor contentAccessor
    ) {
        var iterator = new DocxIterator(contentAccessor).slice(crs, null);
        CommentRangeEnd cre = null;
        CommentReference cr = null;
        var commentId = crs.getId();
        while (iterator.hasNext() && (cr == null || cre == null)) {
            var element = iterator.next();
            if (element instanceof CommentRangeEnd found && cre == null && Objects.equals(found.getId(), commentId)) {
                cre = found;
            }
            else if (element instanceof CommentReference found && cr == null && Objects.equals(found.getId(),
                    commentId)) {
                cr = found;
            }
        }

        if (cre == null) throw new IllegalStateException("Could not find comment range end or reference");

        var comment = comment(document, commentId);


        return new StandardComment(docxPart, (CTSmartTagRun) crs.getParent(), crs, cre, comment, cr);
    }

    private static Comments.Comment comment(WordprocessingMLPackage document, BigInteger commentId) {
        return getCommentsPart(document.getParts()).map(CommentUtil::extractContent)
                                                   .map(Comments::getComment)
                                                   .stream()
                                                   .flatMap(Collection::stream)
                                                   .collect(toMap(Comments.Comment::getId, identity()))
                                                   .get(commentId);
    }
}
