package pro.verron.officestamper.core;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.Placeholder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.WmlFactory.*;

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

    /**
     * Creates a new instance of a StandardComment and initializes its properties
     * including the comment, comment range start, comment range end, and comment reference.
     *
     * @param document    the WordprocessingMLPackage document where the comment will be created
     * @param parent      the parent element (P) to which the comment belongs
     * @param placeholder the placeholder containing the content for the comment
     * @param id          the unique identifier for the comment
     * @return a fully initialized StandardComment object
     */
    public static StandardComment create(
            WordprocessingMLPackage document,
            P parent,
            Placeholder placeholder,
            BigInteger id
    ) {
        var commentWrapper = new StandardComment(document);
        commentWrapper.setComment(newComment(id, placeholder.content()));
        commentWrapper.setCommentRangeStart(newCommentRangeStart(id, parent));
        commentWrapper.setCommentRangeEnd(newCommentRangeEnd(id, parent));
        commentWrapper.setCommentReference(newCommentReference(id, parent));
        return commentWrapper;
    }

    @Override public String toString() {
        return "StandardComment{comment={id=%s, content=%s, children=%s}}}".formatted(comment.getId(),
                comment.getContent()
                       .stream()
                       .map(TextUtils::getText)
                       .collect(Collectors.joining(",")),
                children.size());
    }

    @Override public Placeholder asPlaceholder() {
        String string = this.getComment()
                            .getContent()
                            .stream()
                            .filter(P.class::isInstance)
                            .map(P.class::cast)
                            .map(p -> StandardParagraph.from(new TextualDocxPart(document), p))
                            .map(StandardParagraph::asString)
                            .collect(joining());
        return Placeholders.raw(string);
    }

    @Override public ContentAccessor getParent() {
        return DocumentUtil.findSmallestCommonParent(getCommentRangeStart(), getCommentRangeEnd());
    }

    @Override public List<Object> getElements() {
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

    @Override public CommentRangeEnd getCommentRangeEnd() {
        return commentRangeEnd;
    }

    /**
     * Sets the comment range end for the current comment.
     *
     * @param commentRangeEnd the {@link CommentRangeEnd} object representing the end of the comment range
     */
    public void setCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
        this.commentRangeEnd = commentRangeEnd;
    }

    /**
     * <p>Getter for the field <code>commentRangeStart</code>.</p>
     *
     * @return a {@link CommentRangeStart} object
     */
    @Override public CommentRangeStart getCommentRangeStart() {
        return commentRangeStart;
    }

    /**
     * Sets the starting point of the comment range for the current comment.
     *
     * @param commentRangeStart the {@link CommentRangeStart} object representing the beginning of the comment range
     */
    public void setCommentRangeStart(CommentRangeStart commentRangeStart) {
        this.commentRangeStart = commentRangeStart;
    }

    @Override public CommentReference getCommentReference() {
        return commentReference;
    }

    /**
     * Sets the comment reference for the current comment.
     *
     * @param commentReference the {@link CommentReference} object to associate with this comment
     */
    public void setCommentReference(CommentReference commentReference) {
        this.commentReference = commentReference;
    }

    @Override public Set<Comment> getChildren() {
        return children;
    }

    /**
     * Sets the children of the comment by adding all elements from the provided set
     * to the existing children set.
     *
     * @param children the set of {@link Comment} objects to be added as children
     */
    public void setChildren(Set<Comment> children) {
        this.children.addAll(children);
    }

    @Override public Comments.Comment getComment() {
        return comment;
    }

    /**
     * Sets the comment for the current StandardComment.
     *
     * @param comment the {@link Comments.Comment} object to associate with this StandardComment
     */
    public void setComment(Comments.Comment comment) {
        this.comment = comment;
    }

    @Override public WordprocessingMLPackage getDocument() {
        return document;
    }

}
