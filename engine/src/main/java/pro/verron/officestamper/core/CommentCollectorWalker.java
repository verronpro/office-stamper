package pro.verron.officestamper.core;

import org.docx4j.TextUtils;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

class CommentCollectorWalker
        extends BaseDocumentWalker {
    private static final Logger logger = LoggerFactory.getLogger(CommentCollectorWalker.class);
    private final DocxPart document;
    private final Map<BigInteger, Comment> allComments;
    private final Queue<Comment> stack;
    private final Map<BigInteger, Comment> rootComments;

    private CommentCollectorWalker(
            DocxPart document,
            Map<BigInteger, Comment> rootComments,
            Map<BigInteger, Comment> allComments
    ) {
        super(document);
        this.document = document;
        this.allComments = allComments;
        this.stack = Collections.asLifoQueue(new ArrayDeque<>());
        this.rootComments = rootComments;
    }

    static Map<BigInteger, Comment> collectComments(DocxPart docxPart) {
        var rootComments = new HashMap<BigInteger, Comment>();
        var allComments = new HashMap<BigInteger, Comment>();
        new CommentCollectorWalker(docxPart, rootComments, allComments).walk();

        var commentsPart = docxPart.commentsPart();
        if (commentsPart == null)
            return rootComments;
        var comments = CommentUtil.getComments(commentsPart);

        for (var comment : comments) {
            var commentWrapper = allComments.get(comment.getId());
            if (commentWrapper != null)
                commentWrapper.setComment(comment);
        }
        return cleanMalformedComments(rootComments);
    }

    private static Map<BigInteger, Comment> cleanMalformedComments(Map<BigInteger, Comment> rootComments) {
        Map<BigInteger, Comment> filteredCommentEntries = new HashMap<>();

        rootComments.forEach((key, comment) -> {
            if (isCommentMalformed(comment)) {
                var commentContent = getCommentContent(comment);
                logger.error("Skipping malformed comment, missing range start and/or range end : {}", commentContent);
            }
            else {
                filteredCommentEntries.put(key, comment);
                comment.setChildren(cleanMalformedComments(comment.getChildren()));
            }
        });
        return filteredCommentEntries;
    }

    private static Set<Comment> cleanMalformedComments(Set<Comment> children) {
        return children
                .stream()
                .filter(comment -> {
                    if (isCommentMalformed(comment)) {
                        var commentContent = getCommentContent(comment);
                        logger.error("Skipping malformed comment, missing range start and/or range end : {}",
                                commentContent);
                        return false;
                    }
                    comment.setChildren(cleanMalformedComments(comment.getChildren()));
                    return true;
                })
                .collect(toSet());
    }

    private static boolean isCommentMalformed(Comment comment) {
        return comment.getCommentRangeStart() == null
               || comment.getCommentRangeEnd() == null
               || comment.getComment() == null;
    }

    private static String getCommentContent(Comment comment) {
        return comment.getComment() == null
                ? "<no content>"
                : comment.getComment()
                         .getContent()
                         .stream()
                         .map(TextUtils::getText)
                         .collect(Collectors.joining(""));
    }

    @Override
    protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {
        Comment comment = allComments.get(commentRangeStart.getId());
        if (comment == null) {
            comment = new StandardComment(document.document());
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
        if (comment == null) {
            comment = new StandardComment(document.document());
            allComments.put(commentReference.getId(), comment);
        }
        comment.setCommentReference(commentReference);
    }
}
