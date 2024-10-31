package pro.verron.officestamper.core;

import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;

import java.math.BigInteger;
import java.util.*;

class CommentCollectorWalker
        extends BaseDocumentWalker {
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

        var sourceDocument = docxPart.document();
        CommentUtil.getCommentsPart(sourceDocument.getParts())
                   .map(CommentUtil::extractContent)
                   .map(Comments::getComment)
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(comment -> allComments.containsKey(comment.getId()))
                   .forEach(comment -> allComments.get(comment.getId())
                                                  .setComment(comment));
        return new HashMap<>(rootComments);
    }


    @Override
    protected void onCommentRangeStart(CommentRangeStart commentRangeStart) {
        Comment comment = allComments.get(commentRangeStart.getId());
        if (comment == null) {
            comment = new StandardComment(document.document());
            allComments.put(commentRangeStart.getId(), comment);
            if (stack.isEmpty()) {
                rootComments.put(commentRangeStart.getId(), comment);
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
