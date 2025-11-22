package pro.verron.officestamper.core;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
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
    private final DocxPart docxPart;
    private Comments.Comment comment;
    private CommentRangeStart commentRangeStart;
    private CommentRangeEnd commentRangeEnd;
    private CommentReference commentReference;

    /// Constructs a new StandardComment object.
    ///
    /// @param docxPart the WordprocessingMLPackage document instance
    public StandardComment(DocxPart docxPart) {
        this.docxPart = docxPart;
    }

    /// Creates a new instance of a StandardComment and initializes its properties
    /// including the comment, comment range start, comment range end, and comment reference.
    ///
    /// @param document    the WordprocessingMLPackage document where the comment will be created
    /// @param parent      the parent element (P) to which the comment belongs
    /// @param placeholder the placeholder containing the content for the comment
    /// @param id          the unique identifier for the comment
    /// @return a fully initialized StandardComment object
    public static StandardComment create(
            DocxPart document,
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

    /// Generates a string representation of the StandardComment object, including its ID,
    /// content, and the number of children comments.
    ///
    /// @return a formatted string describing the StandardComment's properties,
    ///         including its ID, content, and the size of its children.
    @Override public String toString() {
        return "StandardComment{comment={id=%s, content=%s, children=%s}}}".formatted(comment.getId(),
                comment.getContent()
                       .stream()
                       .map(TextUtils::getText)
                       .collect(Collectors.joining(",")),
                children.size());
    }

    /// Converts the current comment into a Placeholder representation.
    /// This method processes the content of the associated comment, extracts specific elements
    /// that are instances of the `P` class, transforms them into strings, and combines them
    /// to create a raw placeholder using `Placeholders.raw`.
    ///
    /// @return a [Placeholder] instance containing the combined string representation of the comment content
    @Override public Placeholder asPlaceholder() {
        String string = this.getComment()
                            .getContent()
                            .stream()
                            .filter(P.class::isInstance)
                            .map(P.class::cast)
                            .map(p -> StandardParagraph.from(new TextualDocxPart(docxPart.document()), p))
                            .map(StandardParagraph::asString)
                            .collect(joining());
        return Placeholders.raw(string);
    }

    /// Returns the smallest common parent of the elements defined by the start
    /// and end of the comment range.
    ///
    /// @return the ContentAccessor representing the smallest common parent of
    ///         the comment range start and end, or null if no common parent exists
    @Override public ContentAccessor getParent() {
        return DocumentUtil.findSmallestCommonParent(getCommentRangeStart(), getCommentRangeEnd());
    }

    /// Retrieves a list of elements that exist within the comment's range,
    /// bounded by the start and end of the comment range.
    /// The method iterates through the siblings of the comment's parent content,
    /// collecting elements starting from the range start to the range end.
    ///
    /// @return a list of elements between the comment range start and comment range end
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

    /// Retrieves the CommentRangeEnd object associated with this comment.
    ///
    /// @return the CommentRangeEnd object representing the end of the comment range
    @Override public CommentRangeEnd getCommentRangeEnd() {
        return commentRangeEnd;
    }

    /// Sets the comment range end for the current comment.
    ///
    /// @param commentRangeEnd the [CommentRangeEnd] object representing the end of the comment range
    public void setCommentRangeEnd(CommentRangeEnd commentRangeEnd) {
        this.commentRangeEnd = commentRangeEnd;
    }

    /// Getter for the field <code>commentRangeStart</code>.
    ///
    /// @return a [CommentRangeStart] object
    @Override public CommentRangeStart getCommentRangeStart() {
        return commentRangeStart;
    }

    /// Sets the starting point of the comment range for the current comment.
    ///
    /// @param commentRangeStart the [CommentRangeStart] object representing the beginning of the comment range
    public void setCommentRangeStart(CommentRangeStart commentRangeStart) {
        this.commentRangeStart = commentRangeStart;
    }

    /// Retrieves the comment reference associated with this comment.
    ///
    /// @return the CommentReference object linked to this comment
    @Override public CommentReference getCommentReference() {
        return commentReference;
    }

    /// Sets the comment reference for the current comment.
    ///
    /// @param commentReference the [CommentReference] object to associate with this comment
    public void setCommentReference(CommentReference commentReference) {
        this.commentReference = commentReference;
    }

    /// Retrieves the set of child comments associated with this comment.
    ///
    /// @return a set containing the child comments of the current comment
    @Override public Set<Comment> getChildren() {
        return children;
    }

    /// Sets the children of the comment by adding all elements from the provided set
    /// to the existing children set.
    ///
    /// @param children the set of [Comment] objects to be added as children
    public void setChildren(Set<Comment> children) {
        this.children.addAll(children);
    }

    /// Retrieves the comment associated with this StandardComment.
    ///
    /// @return the Comments.Comment object representing the associated comment
    @Override public Comments.Comment getComment() {
        return comment;
    }

    /// Sets the comment for the current StandardComment.
    ///
    /// @param comment the [Comments.Comment] object to associate with this StandardComment
    public void setComment(Comments.Comment comment) {
        this.comment = comment;
    }

    /// Retrieves the WordprocessingMLPackage document associated with this StandardComment instance.
    ///
    /// @return the WordprocessingMLPackage document associated with this StandardComment instance
    @Override public WordprocessingMLPackage getDocument() {
        return docxPart.document();
    }

}
