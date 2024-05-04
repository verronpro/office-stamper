package pro.verron.docxstamper.core;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;
import pro.verron.docxstamper.api.Comment;
import pro.verron.docxstamper.api.OfficeStamperException;
import pro.verron.docxstamper.api.Placeholder;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
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
    private static final Logger logger = LoggerFactory.getLogger(CommentUtil.class);
    private static final String WORD_COMMENTS_PART_NAME = "/word/comments.xml";

    /**
     * Utility class for handling comments in a DOCX document.
     */
    // TODO: Move to private for next version
    protected CommentUtil() {
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
    public static Optional<Comments.Comment> getCommentAround(
            R run, WordprocessingMLPackage document
    ) {
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
            if (unwrap(contentElement) instanceof CommentRangeStart crs)
                possibleComment = crs;
                // then we check if the child we are looking for is ours
            else if (possibleComment != null && run.equals(contentElement))
                foundChild = true;
                // and then, if we have an end of a comment, we are good!
            else if (possibleComment != null && foundChild && unwrap(
                    contentElement) instanceof CommentRangeEnd) {
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
    public static Optional<Comments.Comment> findComment(
            WordprocessingMLPackage document, BigInteger id
    )
            throws Docx4JException {
        var name = new PartName(WORD_COMMENTS_PART_NAME);
        var parts = document.getParts();
        var wordComments = (CommentsPart) parts.get(name);
        var comments = wordComments.getContents();
        return comments.getComment()
                       .stream()
                       .filter(comment -> comment.getId()
                                                 .equals(id))
                       .findFirst();
    }

    /**
     * Returns the comment string for the given DOCX4J object and document.
     *
     * @param object   the DOCX4J object whose comment to retrieve.
     * @param document the document that contains the object.
     *
     * @return an Expression representing the comment string.
     *
     * @throws OfficeStamperException if an error occurs while retrieving the comment.
     * @deprecated This method's been deprecated since version 1.6.8
     * and will be removed in the future.
     */
    @Deprecated(since = "1.6.8", forRemoval = true)
    public static Placeholder getCommentStringFor(
            ContentAccessor object, WordprocessingMLPackage document
    ) {
        var comment = getCommentFor(object, document)
                .orElseThrow(OfficeStamperException::new);
        return getCommentString(comment);
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
    public static Optional<Comments.Comment> getCommentFor(
            ContentAccessor object, WordprocessingMLPackage document
    ) {
        for (Object contentObject : object.getContent()) {
            if (!(contentObject instanceof CommentRangeStart crs)) continue;
            BigInteger id = crs.getId();
            PartName partName;
            try {
                partName = new PartName(WORD_COMMENTS_PART_NAME);
            } catch (InvalidFormatException e) {
                String message = String.format("Error while searching comment. Skipping object %s.", object);
                throw new OfficeStamperException(message, e);
            }
            CommentsPart commentsPart = (CommentsPart) document.getParts()
                                                               .get(partName);
            Comments comments;
            try {
                comments = commentsPart.getContents();
            } catch (Docx4JException e) {
                throw new OfficeStamperException("error accessing the comments of the document!", e);
            }

            for (Comments.Comment comment : comments.getComment()) {
                var commentId = comment.getId();
                if (commentId.equals(id)) {
                    return Optional.of(comment);
                }
            }
        }
        return Optional.empty();
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
     * Extracts all comments from the given document.
     *
     * @param document the document to extract comments from.
     *
     * @return a map of all comments, with the key being the comment id.
     */
    public static Map<BigInteger, Comment> getComments(
            WordprocessingMLPackage document
    ) {
        Map<BigInteger, Comment> rootComments = new HashMap<>();
        Map<BigInteger, Comment> allComments = new HashMap<>();
        collectCommentRanges(rootComments, allComments, document);
        collectComments(allComments, document);
        return cleanMalformedComments(rootComments);
    }

    /**
     * Returns the string value of the specified comment object.
     *
     * @param items     a {@link List} object
     * @param commentId a {@link BigInteger} object
     */
    public static void deleteCommentFromElement(
            List<Object> items, BigInteger commentId
    ) {
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
                deleteCommentFromElement(ca.getContent(), commentId);
            }
        }
        items.removeAll(elementsToRemove);
    }

    private static Map<BigInteger, Comment> cleanMalformedComments(Map<BigInteger, Comment> rootComments) {
        Map<BigInteger, Comment> filteredCommentEntries = new HashMap<>();

        rootComments.forEach((key, comment) -> {
            if (isCommentMalformed(comment)) {
                logger.error(
                        "Skipping malformed comment, missing range start and/or range end : {}",
                        getCommentContent(comment));
            }
            else {
                filteredCommentEntries.put(key, comment);
                comment.setChildren(cleanMalformedComments(comment.getChildren()));
            }
        });
        return filteredCommentEntries;
    }

    private static Set<Comment> cleanMalformedComments(Set<Comment> children) {
        return children.stream()
                       .filter(comment -> {
                           if (isCommentMalformed(comment)) {
                               logger.error(
                                       "Skipping malformed comment, missing range start and/or range end : {}",
                                       getCommentContent(comment));
                               return false;
                           }
                           comment.setChildren(cleanMalformedComments(comment.getChildren()));
                           return true;
                       })
                       .collect(toSet());
    }

    private static String getCommentContent(Comment comment) {
        return comment.getComment() != null ? comment.getComment()
                                                     .getContent()
                                                     .stream()
                                                     .map(TextUtils::getText)
                                                     .collect(Collectors.joining("")) : "<no content>";
    }

    private static boolean isCommentMalformed(Comment comment) {
        return comment.getCommentRangeStart() == null || comment.getCommentRangeEnd() == null
                || comment.getComment() == null;
    }

    private static void collectCommentRanges(
            Map<BigInteger, Comment> rootComments,
            Map<BigInteger, Comment> allComments,
            WordprocessingMLPackage document
    ) {
        Queue<Comment> stack = Collections.asLifoQueue(new ArrayDeque<>());
        DocumentWalker documentWalker = new BaseDocumentWalker(document.getMainDocumentPart()) {
            @Override
            protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {
                Comment comment = allComments.get(commentRangeStart.getId());
                if (comment == null) {
                    comment = new StandardComment(document);
                    allComments.put(commentRangeStart.getId(), comment);
                    if (stack.isEmpty()) {
                        rootComments.put(commentRangeStart.getId(),
                                comment);
                    }
                    else {
                        stack.peek()
                             .getChildren()
                             .add(comment);
                    }
                }
                comment.setCommentRangeStart(commentRangeStart);
                stack.add(comment);
            }

            @Override
            protected void onCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
                Comment comment = allComments.get(commentRangeEnd.getId());
                if (comment == null)
                    throw new OfficeStamperException("Found a comment range end before the comment range start !");

                comment.setCommentRangeEnd(commentRangeEnd);

                if (stack.isEmpty()) return;

                var peek = stack.peek();
                if (peek.equals(comment))
                    stack.remove();
                else throw new OfficeStamperException("Cannot figure which comment contains the other !");
            }

            @Override
            protected void onCommentReference(R.CommentReference commentReference) {
                Comment comment = allComments.get(commentReference.getId());
                if (comment == null)
                    throw new OfficeStamperException("Found a comment reference before the comment range start !");
                comment.setCommentReference(commentReference);
            }
        };
        documentWalker.walk();
    }

    private static void collectComments(
            Map<BigInteger, Comment> allComments,
            WordprocessingMLPackage document
    ) {
        try {
            var documentParts = document.getParts();
            var commentsPart = (CommentsPart) documentParts.get(new PartName(WORD_COMMENTS_PART_NAME));
            if (commentsPart == null) return;
            var commentsPartContents = commentsPart.getContents();
            for (var comment : commentsPartContents.getComment()) {
                var commentWrapper = allComments.get(comment.getId());
                if (commentWrapper != null)
                    commentWrapper.setComment(comment);
            }
        } catch (Docx4JException e) {
            throw new IllegalStateException(e);
        }
    }

    static void removeCommentAnchorsFromFinalElements(
            Comment comment,
            List<Object> elements
    ) {
        var docx4jComment = comment.getComment();
        var commentId = docx4jComment.getId();
        deleteCommentFromElement(elements, commentId);
    }
}
