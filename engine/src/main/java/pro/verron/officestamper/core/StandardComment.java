package pro.verron.officestamper.core;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.api.Comment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>CommentWrapper class.</p>
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.2
 */
public class StandardComment
        implements Comment {
    private final Set<Comment> children = new HashSet<>();
    private final WordprocessingMLPackage document;
    private Comments.Comment comment;
    private CommentRangeStart commentRangeStart;
    private CommentRangeEnd commentRangeEnd;
    private CommentReference commentReference;

    /**
     * Constructs a new StandardComment object.
     *
     * @param document the WordprocessingMLPackage document instance
     */
    public StandardComment(WordprocessingMLPackage document) {
        this.document = document;
    }

    @Override public String toString() {
        return "StandardComment{comment={id=%s, content=%s, children=%s}}}".formatted(
                comment.getId(),
                comment.getContent().stream().map(TextUtils::getText).collect(Collectors.joining(",")),
                children.size());
    }

    /**
     * <p>getParent.</p>
     *
     * @return the comment's author.
     */
    @Override
    public ContentAccessor getParent() {
        return DocumentUtil.findSmallestCommonParent(getCommentRangeStart(), getCommentRangeEnd());
    }

    /**
     * @return the elements in the document that are between the comment range anchors.
     */
    @Override
    public List<Object> getElements() {
        List<Object> elements = new ArrayList<>();
        boolean startFound = false;
        boolean endFound = false;
        var siblings = getParent().getContent();
        for (Object element : siblings) {
            startFound = startFound || DocumentUtil.depthElementSearch(getCommentRangeStart(), element);
            if (startFound && !endFound) elements.add(element);
            endFound = endFound || DocumentUtil.depthElementSearch(getCommentRangeEnd(), element);
        }
        return elements;
    }

    /**
     * <p>Getter for the field <code>commentRangeEnd</code>.</p>
     *
     * @return a {@link CommentRangeEnd} object
     */
    @Override
    public CommentRangeEnd getCommentRangeEnd() {
        return commentRangeEnd;
    }

    public void setCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
        this.commentRangeEnd = commentRangeEnd;
    }

    /**
     * <p>Getter for the field <code>commentRangeStart</code>.</p>
     *
     * @return a {@link CommentRangeStart} object
     */
    @Override
    public CommentRangeStart getCommentRangeStart() {
        return commentRangeStart;
    }

    public void setCommentRangeStart(CommentRangeStart commentRangeStart) {
        this.commentRangeStart = commentRangeStart;
    }

    /**
     * <p>Getter for the field <code>commentReference</code>.</p>
     *
     * @return a {@link CommentReference} object
     */
    @Override
    public CommentReference getCommentReference() {
        return commentReference;
    }

    public void setCommentReference(CommentReference commentReference) {
        this.commentReference = commentReference;
    }

    /**
     * <p>Getter for the field <code>children</code>.</p>
     *
     * @return a {@link Set} object
     */
    @Override
    public Set<Comment> getChildren() {
        return children;
    }

    public void setChildren(Set<Comment> children) {
        this.children.addAll(children);
    }

    /**
     * <p>Getter for the field <code>comment</code>.</p>
     *
     * @return a {@link Comments.Comment} object
     */
    @Override
    public Comments.Comment getComment() {
        return comment;
    }

    public void setComment(Comments.Comment comment) {
        this.comment = comment;
    }

    @Override public WordprocessingMLPackage getDocument() {
        return document;
    }

}
