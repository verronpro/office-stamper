package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Placeholder;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.docx4j.XmlUtils.unwrap;

/**
 * Utility class for working with comments in a DOCX document.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class CommentUtil {
    private static final PartName WORD_COMMENTS_PART_NAME;
    private static final Logger logger = LoggerFactory.getLogger(CommentUtil.class);

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

    /**
     * Returns the comment the given DOCX4J object is commented with.
     *
     * @param run      the DOCX4J object whose comment to retrieve.
     * @param document the document that contains the object.
     *
     * @return Optional of the comment, if found, Optional.empty() otherwise.
     */
    public static Optional<Comments.Comment> getCommentAround(R run, WordprocessingMLPackage document) {
        ContentAccessor parent = (ContentAccessor) ((Child) run).getParent();
        if (parent == null) return Optional.empty();

        try {
            return getComment(run, document, parent);
        } catch (Docx4JException e) {
            throw new OfficeStamperException("error accessing the comments of the document!", e);
        }
    }

    private static Optional<Comments.Comment> getComment(
            R run, WordprocessingMLPackage document, ContentAccessor parent
    )
            throws Docx4JException {
        CommentRangeStart possibleComment = null;
        boolean foundChild = false;
        for (Object contentElement : parent.getContent()) {
            // so first we look for the start of the comment
            if (unwrap(contentElement) instanceof CommentRangeStart crs) possibleComment = crs;
                // then we check if the child we are looking for is ours
            else if (possibleComment != null && run.equals(contentElement)) foundChild = true;
                // and then, if we have an end of a comment, we are good!
            else if (possibleComment != null && foundChild && unwrap(contentElement) instanceof CommentRangeEnd) {
                try {
                    var id = possibleComment.getId();
                    return findComment(document, id);
                } catch (InvalidFormatException e) {
                    var format = "Error while searching comment. Skipping run %s.";
                    var message = String.format(format, run);
                    logger.warn(message, e);
                }
            }
            // else restart
            else {
                possibleComment = null;
                foundChild = false;
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a comment with the given ID in the specified WordprocessingMLPackage document.
     *
     * @param document the WordprocessingMLPackage document to search for the comment
     * @param id       the ID of the comment to find
     *
     * @return an Optional containing the Comment if found, or an empty Optional if not found
     *
     * @throws Docx4JException if an error occurs while searching for the comment
     */
    private static Optional<Comments.Comment> findComment(WordprocessingMLPackage document, BigInteger id)
            throws Docx4JException {
        var wordComments = getCommentsPart(document.getParts());
        var comments = wordComments.getContents();
        return comments.getComment()
                       .stream()
                       .filter(comment -> id.equals(comment.getId()))
                       .findFirst();
    }

    static CommentsPart getCommentsPart(Parts parts) {
        return (CommentsPart) parts.get(WORD_COMMENTS_PART_NAME);
    }

    /**
     * Returns the first comment found for the given docx object. Note that an object is
     * only considered commented if the comment STARTS within the object. Comments
     * spanning several objects are not supported by this method.
     *
     * @param object   the object whose comment to load.
     * @param document the document in which the object is embedded (needed to load the
     *                 comment from the comments.xml part).
     *
     * @return the concatenated string of all text paragraphs within the
     * comment or null if the specified object is not commented.
     */
    public static Optional<Comments.Comment> getCommentFor(ContentAccessor object, WordprocessingMLPackage document) {
        for (Object contentObject : object.getContent()) {
            if (!(contentObject instanceof CommentRangeStart crs)) continue;
            BigInteger id = crs.getId();
            CommentsPart commentsPart = getCommentsPart(document.getParts());
            var comments = getComments(commentsPart);

            for (Comments.Comment comment : comments) {
                var commentId = comment.getId();
                if (commentId.equals(id)) {
                    return Optional.of(comment);
                }
            }
        }
        return Optional.empty();
    }

    public static List<Comments.Comment> getComments(CommentsPart commentsPart) {
        try {
            return commentsPart.getContents()
                               .getComment();
        } catch (Docx4JException e) {
            throw new OfficeStamperException("error accessing the comments of the document!", e);
        }
    }

    /**
     * Returns the string value of the specified comment object.
     *
     * @param comment a {@link Comments.Comment} object
     *
     * @return a {@link String} object
     */
    public static Placeholder getCommentString(Comments.Comment comment) {
        StringBuilder builder = new StringBuilder();
        for (Object commentChildObject : comment.getContent()) {
            if (commentChildObject instanceof P p) {
                builder.append(new StandardParagraph(p).asString());
            }
        }
        String string = builder.toString();
        return Placeholders.raw(string);
    }

    /**
     * Returns the string value of the specified comment object.
     *
     * @param comment a {@link Comment} object
     */
    public static void deleteComment(Comment comment) {
        CommentRangeEnd end = comment.getCommentRangeEnd();
        if (end != null) {
            ContentAccessor endParent = (ContentAccessor) end.getParent();
            endParent.getContent()
                     .remove(end);
        }
        CommentRangeStart start = comment.getCommentRangeStart();
        if (start != null) {
            ContentAccessor startParent = (ContentAccessor) start.getParent();
            startParent.getContent()
                       .remove(start);
        }
        R.CommentReference reference = comment.getCommentReference();
        if (reference != null) {
            ContentAccessor referenceParent = (ContentAccessor) reference.getParent();
            referenceParent.getContent()
                           .remove(reference);
        }
    }

    /**
     * Returns the string value of the specified comment object.
     *
     * @param items     a {@link List} object
     * @param commentId a {@link BigInteger} object
     */
    public static void deleteCommentFromElements(List<Object> items, BigInteger commentId) {
        List<Object> elementsToRemove = new ArrayList<>();
        for (Object item : items) {
            Object unwrapped = unwrap(item);
            if (unwrapped instanceof CommentRangeStart crs) {
                var id = crs.getId();
                if (id.equals(commentId)) {
                    elementsToRemove.add(item);
                }
            }
            else if (unwrapped instanceof CommentRangeEnd cre) {
                var id = cre.getId();
                if (id.equals(commentId)) {
                    elementsToRemove.add(item);
                }
            }
            else if (unwrapped instanceof R.CommentReference rcr) {
                var id = rcr.getId();
                if (id.equals(commentId)) {
                    elementsToRemove.add(item);
                }
            }
            else if (unwrapped instanceof ContentAccessor ca) {
                deleteCommentFromElements(ca.getContent(), commentId);
            }
        }
        items.removeAll(elementsToRemove);
    }

    private static void deleteCommentFromElements(
            Comment comment, List<Object> elements
    ) {
        var docx4jComment = comment.getComment();
        var commentId = docx4jComment.getId();
        deleteCommentFromElements(elements, commentId);
    }

    /**
     * Creates a sub Word document
     * by extracting a specified comment and its associated content from the original document.
     *
     * @param comment The comment to be extracted from the original document.
     *
     * @return The sub Word document containing the content of the specified comment.
     */
    public static WordprocessingMLPackage createSubWordDocument(Comment comment) {
        var elements = comment.getElements();

        var target = createWordPackageWithCommentsPart();

        // copy the elements without comment range anchors
        var finalElements = elements.stream()
                                    .map(XmlUtils::deepCopy)
                                    .collect(Collectors.toCollection(ArrayList::new));
        deleteCommentFromElements(comment, finalElements);
        target.getMainDocumentPart()
              .getContent()
              .addAll(finalElements);

        // copy the images from parent document using the original repeat elements
        var wmlObjectFactory = Context.getWmlObjectFactory();
        var fakeBody = wmlObjectFactory.createBody();
        fakeBody.getContent()
                .addAll(elements);
        DocumentUtil.walkObjectsAndImportImages(fakeBody, comment.getDocument(), target);

        var comments = extractComments(comment.getChildren());
        target.getMainDocumentPart()
              .getCommentsPart()
              .setContents(comments);
        return target;
    }

    private static WordprocessingMLPackage createWordPackageWithCommentsPart() {
        try {
            CommentsPart targetCommentsPart = new CommentsPart();
            var target = WordprocessingMLPackage.createPackage();
            var mainDocumentPart = target.getMainDocumentPart();
            mainDocumentPart.addTargetPart(targetCommentsPart);
            return target;
        } catch (InvalidFormatException e) {
            throw new OfficeStamperException("Failed to create a Word package with comment Part", e);
        }
    }

    private static Comments extractComments(Set<Comment> commentChildren) {
        var wmlObjectFactory = Context.getWmlObjectFactory();
        var comments = wmlObjectFactory.createComments();
        var commentList = comments.getComment();

        var queue = new ArrayDeque<>(commentChildren);
        while (!queue.isEmpty()) {
            var comment = queue.remove();
            commentList.add(comment.getComment());
            if (comment.getChildren() != null) {
                queue.addAll(comment.getChildren());
            }
        }
        return comments;
    }
}
