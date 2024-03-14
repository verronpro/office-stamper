package pro.verron.docxstamper.core;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.docx4j.wml.Comments.Comment;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.util.walk.BaseDocumentWalker;
import org.wickedsource.docxstamper.util.walk.DocumentWalker;
import pro.verron.docxstamper.api.CommentWrapper;
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

    private CommentUtil() {
        throw new DocxStamperException("Utility class shouldn't be instantiated");
    }

    /**
     * Returns the comment the given DOCX4J object is commented with.
     *
     * @param run      the DOCX4J object whose comment to retrieve.
     * @param document the document that contains the object.
     * @return Optional of the comment, if found, Optional.empty() otherwise.
     */
    public static Optional<Comment> getCommentAround(
            R run, WordprocessingMLPackage document
    ) {
        if (run == null) return Optional.empty();

        ContentAccessor parent = (ContentAccessor) ((Child) run).getParent();
        if (parent == null) return Optional.empty();

        try {
            return getComment(run, document, parent);
        } catch (Docx4JException e) {
            throw new DocxStamperException(
                    "error accessing the comments of the document!",
                    e);
        }
    }

    private static Optional<Comment> getComment(
            R run, WordprocessingMLPackage document, ContentAccessor parent
    ) throws Docx4JException {
        CommentRangeStart possibleComment = null;
        boolean foundChild = false;
        for (Object contentElement : parent.getContent()) {
            // so first we look for the start of the comment
            if (unwrap(contentElement) instanceof CommentRangeStart crs)
                possibleComment = crs;
                // then we check if the child we are looking for is ours
            else if (possibleComment != null && run.equals(contentElement))
                foundChild = true;
                // and then if we have an end of a comment we are good!
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
     * @return an Optional containing the Comment if found, or an empty Optional if not found
     * @throws Docx4JException if an error occurs while searching for the comment
     */
    public static Optional<Comment> findComment(
            WordprocessingMLPackage document, BigInteger id
    ) throws Docx4JException {
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
     * @return an Expression representing the comment string.
     * @throws DocxStamperException if an error occurs while retrieving the comment.
     * @deprecated This method's been deprecated since version 1.6.8
     * and will be removed in the future.
     */
    @Deprecated(since = "1.6.8", forRemoval = true)
    public static Placeholder getCommentStringFor(
            ContentAccessor object, WordprocessingMLPackage document
    ) {
        Comment comment = getCommentFor(object, document).orElseThrow();
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
     * @return the concatenated string of all text paragraphs within the
     * comment or null if the specified object is not commented.
     */
    public static Optional<Comment> getCommentFor(
            ContentAccessor object, WordprocessingMLPackage document
    ) {
        for (Object contentObject : object.getContent()) {
            if (!(contentObject instanceof CommentRangeStart crs)) continue;
            BigInteger id = crs.getId();
            PartName partName;
            try {
                partName = new PartName(WORD_COMMENTS_PART_NAME);
            } catch (InvalidFormatException e) {
                String message = String.format(
                        "Error while searching comment. Skipping object %s.",
                        object);
                throw new DocxStamperException(message, e);
            }
            CommentsPart commentsPart = (CommentsPart) document.getParts()
                    .get(partName);
            Comments comments;
            try {
                comments = commentsPart.getContents();
            } catch (Docx4JException e) {
                throw new DocxStamperException(
                        "error accessing the comments of the document!",
                        e);
            }

            for (Comment comment : comments.getComment()) {
                if (comment.getId()
                        .equals(id)) {
                    return Optional.of(comment);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the string value of the specified comment object.
     *
     * @param comment a {@link Comment} object
     * @return a {@link String} object
     */
    public static Placeholder getCommentString(Comment comment) {
        StringBuilder builder = new StringBuilder();
        for (Object commentChildObject : comment.getContent()) {
            if (commentChildObject instanceof P p) {
                builder.append(new Paragraph(p).getText());
            }
        }
        String string = builder.toString();
        return Expressions.raw(string);
    }

    /**
     * Returns the string value of the specified comment object.
     *
     * @param comment a {@link CommentWrapper} object
     */
    public static void deleteComment(CommentWrapper comment) {
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
    public static void deleteCommentFromElement(
            List<Object> items, BigInteger commentId
    ) {
        List<Object> elementsToRemove = new ArrayList<>();
        for (Object item : items) {
            Object unwrapped = unwrap(item);
            if (unwrapped instanceof CommentRangeStart crs) {
                if (crs.getId()
                        .equals(commentId)) {
                    elementsToRemove.add(item);
                }
            } else if (unwrapped instanceof CommentRangeEnd cre) {
                if (cre.getId()
                        .equals(commentId)) {
                    elementsToRemove.add(item);
                }
            } else if (unwrapped instanceof R.CommentReference rcr) {
                if (rcr.getId()
                        .equals(commentId)) {
                    elementsToRemove.add(item);
                }
            } else if (unwrapped instanceof ContentAccessor ca) {
                deleteCommentFromElement(ca.getContent(), commentId);
            }
        }
        items.removeAll(elementsToRemove);
    }

    /**
     * Extracts all comments from the given document.
     *
     * @param document the document to extract comments from.
     * @return a map of all comments, with the key being the comment id.
     */
    public static Map<BigInteger, CommentWrapper> getComments(
            WordprocessingMLPackage document
    ) {
        Map<BigInteger, CommentWrapper> rootComments = new HashMap<>();
        Map<BigInteger, CommentWrapper> allComments = new HashMap<>();
        collectCommentRanges(rootComments, allComments, document);
        collectComments(allComments, document);
        return cleanMalformedComments(rootComments);
    }

    private static Map<BigInteger, CommentWrapper> cleanMalformedComments(Map<BigInteger, CommentWrapper> rootComments) {
        Map<BigInteger, CommentWrapper> filteredCommentEntries = new HashMap<>();

        rootComments.forEach((key, comment) -> {
            if (isCommentMalformed(comment)) {
                logger.error(
                        "Skipping malformed comment, missing range start and/or range end : {}",
                        getCommentContent(comment));
            } else {
                filteredCommentEntries.put(key, comment);
                comment.setChildren(cleanMalformedComments(comment.getChildren()));
            }
        });
        return filteredCommentEntries;
    }

    private static Set<CommentWrapper> cleanMalformedComments(Set<CommentWrapper> children) {
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

    private static String getCommentContent(CommentWrapper comment) {
        return comment.getComment() != null ? comment.getComment()
                .getContent()
                .stream()
                .map(TextUtils::getText)
                .collect(Collectors.joining("")) : "<no content>";
    }

    private static boolean isCommentMalformed(CommentWrapper comment) {
        return comment.getCommentRangeStart() == null || comment.getCommentRangeEnd() == null || comment.getComment() == null;
    }

    private static void collectCommentRanges(
            Map<BigInteger, CommentWrapper> rootComments,
            Map<BigInteger, CommentWrapper> allComments,
            WordprocessingMLPackage document
    ) {
        Queue<CommentWrapper> stack = Collections.asLifoQueue(new ArrayDeque<>());
        DocumentWalker documentWalker = new BaseDocumentWalker(document.getMainDocumentPart()) {
            @Override
            protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {
                CommentWrapper commentWrapper = allComments.get(
                        commentRangeStart.getId());
                if (commentWrapper == null) {
                    commentWrapper = new DefaultCommentWrapper();
                    allComments.put(commentRangeStart.getId(), commentWrapper);
                    if (stack.isEmpty()) {
                        rootComments.put(commentRangeStart.getId(),
                                         commentWrapper);
                    } else {
                        stack.peek()
                                .getChildren()
                                .add(commentWrapper);
                    }
                }
                commentWrapper.setCommentRangeStart(commentRangeStart);
                stack.add(commentWrapper);
            }

            @Override
            protected void onCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
                CommentWrapper commentWrapper = allComments.get(commentRangeEnd.getId());
                if (commentWrapper == null) throw new DocxStamperException(
                        "Found a comment range end before the comment range start !");

                commentWrapper.setCommentRangeEnd(commentRangeEnd);

                if (stack.isEmpty()) return;

                if (stack.peek()
                        .equals(commentWrapper)) stack.remove();
                else throw new DocxStamperException(
                        "Cannot figure which comment contains the other !");
            }

            @Override
            protected void onCommentReference(R.CommentReference commentReference) {
                CommentWrapper commentWrapper = allComments.get(commentReference.getId());
                if (commentWrapper == null) throw new DocxStamperException(
                        "Found a comment reference before the comment range start !");
                commentWrapper.setCommentReference(commentReference);
            }
        };
        documentWalker.walk();
    }

    private static void collectComments(
            Map<BigInteger, CommentWrapper> allComments,
            WordprocessingMLPackage document
    ) {
        try {
            CommentsPart commentsPart = (CommentsPart) document.getParts()
                    .get(new PartName(WORD_COMMENTS_PART_NAME));
            if (commentsPart == null) {return;}
            var commentsPartContents = commentsPart.getContents();
            for (var comment : commentsPartContents.getComment()) {
                var commentWrapper = allComments.get(comment.getId());
                if (commentWrapper != null) {
                    commentWrapper.setComment(comment);
                }
            }
        } catch (Docx4JException e) {
            throw new IllegalStateException(e);
        }
    }

}
