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

/// CommentWrapper class.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.2
public class StandardComment
        implements Comment {
    private final Set<Comment> children = new HashSet<>();
    private final WordprocessingMLPackage document;
    private Comments.Comment comment;
    private CommentRangeStart commentRangeStart;
    private CommentRangeEnd commentRangeEnd;
    private CommentReference commentReference;

    /// Constructs a new StandardComment object.
    ///
    /// @param document the WordprocessingMLPackage document instance
    public StandardComment(WordprocessingMLPackage document) {
        this.document = document;
    }

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

    /// @return the comment's author.
    @Override public ContentAccessor getParent() {
        return DocumentUtil.findSmallestCommonParent(getCommentRangeStart(), getCommentRangeEnd());
    }

    /// @return the elements in the document that are between the comment range anchors.
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

    /// Getter for the field <code>commentRangeEnd</code>.
    ///
    /// @return a [CommentRangeEnd] object
    @Override public CommentRangeEnd getCommentRangeEnd() {
        return commentRangeEnd;
    }

    public void setCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
        this.commentRangeEnd = commentRangeEnd;
    }

    /// Getter for the field <code>commentRangeStart</code>.
    ///
    /// @return a [CommentRangeStart] object
    @Override public CommentRangeStart getCommentRangeStart() {
        return commentRangeStart;
    }

    public void setCommentRangeStart(CommentRangeStart commentRangeStart) {
        this.commentRangeStart = commentRangeStart;
    }

    /// Getter for the field <code>commentReference</code>.
    ///
    /// @return a [CommentReference] object
    @Override public CommentReference getCommentReference() {
        return commentReference;
    }

    public void setCommentReference(CommentReference commentReference) {
        this.commentReference = commentReference;
    }

    /// Getter for the field <code>children</code>.
    ///
    /// @return a [Set] object
    @Override public Set<Comment> getChildren() {
        return children;
    }

    public void setChildren(Set<Comment> children) {
        this.children.addAll(children);
    }

    /// Getter for the field <code>comment</code>.
    ///
    /// @return a [Comments.Comment] object
    @Override public Comments.Comment getComment() {
        return comment;
    }

    public void setComment(Comments.Comment comment) {
        this.comment = comment;
    }

    @Override public WordprocessingMLPackage getDocument() {
        return document;
    }
}
