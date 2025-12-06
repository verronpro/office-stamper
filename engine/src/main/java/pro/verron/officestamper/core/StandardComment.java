package pro.verron.officestamper.core;

import org.docx4j.TextUtils;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Paragraph;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.WmlFactory.*;

/// Standard implementation of the [Comment] interface. Represents a comment in a DOCX document with its associated
/// range markers and content.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.2
public class StandardComment
        implements Comment {
    private static final Logger log = LoggerFactory.getLogger(StandardComment.class);
    private final Set<Comment> children = new HashSet<>();
    private final DocxPart part;
    private Comments.Comment comment;
    private CommentRangeStart commentRangeStart;
    private CommentRangeEnd commentRangeEnd;
    private CommentReference commentReference;

    /// Constructs a new [StandardComment] object.
    ///
    /// @param part the [DocxPart] representing the document section this comment belongs to
    public StandardComment(DocxPart part) {
        this.part = part;
    }

    /// Creates a new instance of [StandardComment] and initializes it with the given parameters, including a comment,
    /// comment range start, comment range end, and a comment reference.
    ///
    /// @param document the [DocxPart] representing the document to which this comment belongs
    /// @param parent the [ContentAccessor] representing the parent content of the comment range
    /// @param expression the [String] content to be included in the comment
    /// @param id the unique [BigInteger] identifier for the comment
    ///
    /// @return a [StandardComment] instance initialized with the specified parameters
    public static StandardComment create(DocxPart document, ContentAccessor parent, String expression, BigInteger id) {
        var commentWrapper = new StandardComment(document);
        commentWrapper.setComment(newComment(id, expression));
        commentWrapper.setCommentRangeStart(newCommentRangeStart(id, parent));
        commentWrapper.setCommentRangeEnd(newCommentRangeEnd(id, parent));
        commentWrapper.setCommentReference(newCommentReference(id, parent));
        return commentWrapper;
    }

    /// Generates a string representation of the [StandardComment] object, including its ID, content, and the amount
    /// children comments.
    ///
    /// @return a formatted string describing the [StandardComment]'s properties, including its ID, content, and the
    ///         size of its children.
    @Override
    public String toString() {
        return "StandardComment{comment={id=%s, content=%s, children=%s}}}".formatted(comment.getId(),
                comment.getContent()
                       .stream()
                       .map(TextUtils::getText)
                       .collect(Collectors.joining(",")),
                children.size());
    }

    /// {@inheritDoc}
    @Override
    public Paragraph getParagraph() {
        var parent = commentRangeStart.getParent();
        return StandardParagraph.from(part, parent);
    }

    /// {@inheritDoc}
    @Override
    public CommentRangeStart getCommentRangeStart() {
        return commentRangeStart;
    }

    /// Sets the starting point of the comment range for the current comment.
    ///
    /// @param commentRangeStart the [CommentRangeStart] object representing the beginning of the comment range
    public void setCommentRangeStart(CommentRangeStart commentRangeStart) {
        this.commentRangeStart = commentRangeStart;
    }

    @Override
    public ContentAccessor getParent() {
        return DocumentUtil.findSmallestCommonParent(commentRangeStart, getCommentRangeEnd());
    }

    /// {@inheritDoc}
    @Override
    public List<Object> getElements() {
        List<Object> elements = new ArrayList<>();
        boolean startFound = false;
        boolean endFound = false;
        var siblings = getParent().getContent();
        for (Object element : siblings) {
            startFound = startFound || DocumentUtil.depthElementSearch(commentRangeStart, element);
            if (startFound && !endFound) elements.add(element);
            endFound = endFound || DocumentUtil.depthElementSearch(getCommentRangeEnd(), element);
        }
        return elements;
    }

    @Override
    public CommentRangeEnd getCommentRangeEnd() {
        return commentRangeEnd;
    }

    /// Sets the comment range end for the current comment.
    ///
    /// @param commentRangeEnd the [CommentRangeEnd] object representing the end of the comment range
    public void setCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
        this.commentRangeEnd = commentRangeEnd;
    }

    /// {@inheritDoc}
    @Override
    public CommentReference getCommentReference() {
        return commentReference;
    }

    /// Sets the comment reference for the current comment.
    ///
    /// @param commentReference the [CommentReference] object to associate with this comment
    public void setCommentReference(CommentReference commentReference) {
        this.commentReference = commentReference;
    }

    /// {@inheritDoc}
    @Override
    public Set<Comment> getChildren() {
        return children;
    }

    /// {@inheritDoc}
    @Override
    public Comments.Comment getComment() {
        return comment;
    }

    /// Sets the comment for the current [StandardComment].
    ///
    /// @param comment the [Comments.Comment] object to associate with this [StandardComment]
    public void setComment(Comments.Comment comment) {
        this.comment = comment;
    }

    /// {@inheritDoc}
    @Override
    public String expression() {
        return this.getComment()
                   .getContent()
                   .stream()
                   .filter(P.class::isInstance)
                   .map(P.class::cast)
                   .map(p -> StandardParagraph.from(new TextualDocxPart(part.document()), p))
                   .map(StandardParagraph::asString)
                   .collect(joining());
    }

    /// {@inheritDoc}
    ///
    /// We expects the author field of the comment to be an integer representing the context ID. If it is not found,
    /// then we return the root context with index 0.
    @Override
    public String getContextKey() {
        var author = comment.getAuthor();
        if (author == null) return String.valueOf(0);
        try {
            return String.valueOf(Integer.parseInt(author));
        } catch (NumberFormatException _) {
            log.debug("Expected an context id in the author field: found '{}'", author);
            return String.valueOf(0);
        }
    }

    @Override
    public void setContextKey(String contextKey) {
        comment.setAuthor(contextKey);
    }

    /// Adds a [Comment] to this comment children set.
    ///
    /// @param comment the child comment to be added
    public void addChild(Comment comment) {
        children.add(comment);
    }


}
