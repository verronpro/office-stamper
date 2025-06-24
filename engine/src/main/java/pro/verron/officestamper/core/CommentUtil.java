package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.OfficeStamperException;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.docx4j.XmlUtils.unwrap;
import static pro.verron.officestamper.utils.WmlFactory.newBody;
import static pro.verron.officestamper.utils.WmlFactory.newComments;

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
        ContentAccessor parent = (ContentAccessor) run.getParent();
        if (parent == null) return Optional.empty();
        return getComment(run, document, parent);
    }

    private static Optional<Comments.Comment> getComment(
            R run, WordprocessingMLPackage document, ContentAccessor parent
    ) {
        CommentRangeStart possibleComment = null;
        boolean foundChild = false;
        for (Object contentElement : parent.getContent()) {
            // so first we look for the start of the comment
            if (unwrap(contentElement) instanceof CommentRangeStart crs) possibleComment = crs;
                // then we check if the child we are looking for is ours
            else if (possibleComment != null && run.equals(contentElement)) foundChild = true;
                // and then, if we have an end of a comment, we are good!
            else if (possibleComment != null && foundChild && unwrap(contentElement) instanceof CommentRangeEnd) {
                return findComment(document, possibleComment.getId());
            }
            // else restart
            else {
                possibleComment = null;// TODO There is  bug here when looking for a commented run and the run has
                // ProofErr issues
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
     */
    private static Optional<Comments.Comment> findComment(WordprocessingMLPackage document, BigInteger id) {
        return getCommentsPart(document.getParts()).map(CommentUtil::extractContent)
                                                   .map(Comments::getComment)
                                                   .stream()
                                                   .flatMap(Collection::stream)
                                                   .filter(comment -> id.equals(comment.getId()))
                                                   .findFirst();

    }

    /**
     * Retrieves the comment associated with a given paragraph content within a WordprocessingMLPackage document.
     *
     * @param paragraphContent the content of the paragraph to search for a comment.
     * @param document         the WordprocessingMLPackage document containing the paragraph and its comments.
     *
     * @return an Optional containing the found comment, or Optional.empty() if no comment is associated with the given
     * paragraph content.
     */
    public static Collection<Comments.Comment> getCommentFor(
            List<Object> paragraphContent, WordprocessingMLPackage document
    ) {
        var comments = getCommentsPart(document.getParts()).map(CommentUtil::extractContent)
                                                           .map(Comments::getComment)
                                                           .stream()
                                                           .flatMap(Collection::stream)
                                                           .toList();

        return paragraphContent.stream()
                               .filter(CommentRangeStart.class::isInstance)
                               .map(CommentRangeStart.class::cast)
                               .map(CommentRangeStart::getId)
                               .flatMap(commentId -> findCommentById(comments, commentId).stream())
                               .toList();
    }

    /**
     * Retrieves the CommentsPart from the given Parts object.
     *
     * @param parts the Parts object containing the various parts of the document.
     *
     * @return an Optional containing the CommentsPart if found, or an empty Optional if not found.
     */
    public static Optional<CommentsPart> getCommentsPart(Parts parts) {
        return Optional.ofNullable((CommentsPart) parts.get(WORD_COMMENTS_PART_NAME));
    }

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
            else if (unwrapped instanceof SdtRun sdtRun) {
                deleteCommentFromElements(sdtRun.getSdtContent()
                                                .getContent(), commentId);
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
        var fakeBody = newBody(elements);
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
        var list = new ArrayList<Comments.Comment>();
        var queue = new ArrayDeque<>(commentChildren);
        while (!queue.isEmpty()) {
            var comment = queue.remove();
            list.add(comment.getComment());
            if (comment.getChildren() != null) {
                queue.addAll(comment.getChildren());
            }
        }
        return newComments(list);
    }
}
