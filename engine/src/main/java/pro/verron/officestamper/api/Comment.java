package pro.verron.officestamper.api;

import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R.CommentReference;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/// The Comment interface provides methods for managing comments in a document.
public interface Comment {

    Paragraph getParagraph();

    /// Retrieves the CommentRangeStart object associated with this comment.
    ///
    /// @return the [CommentRangeStart] object associated with this comment
    @Nullable CommentRangeStart getCommentRangeStart();

    /// Retrieves the parent of the comment.
    ///
    /// @return the parent of the comment
    ContentAccessor getParent();

    /// Retrieves the elements in the document that are between the comment range anchors.
    ///
    /// @return a list of objects representing the elements between the comment range anchors.
    List<Object> getElements();

    /// Retrieves the [CommentRangeEnd] object associated with this comment.
    ///
    /// @return the [CommentRangeEnd] object associated with this comment
    @Nullable CommentRangeEnd getCommentRangeEnd();

    /// Retrieves the [CommentReference] object associated with this comment.
    ///
    /// @return the [CommentReference] object associated with this comment
    @Nullable CommentReference getCommentReference();

    /// Retrieves the children of the comment.
    ///
    /// @return a set of Comment objects representing the children of the comment
    Set<Comment> getChildren();

    /// Retrieves the comment associated with this object.
    ///
    /// @return the comment associated with this object
    Comments.@Nullable Comment getComment();

    /// Retrieves the expression associated with the implementing entity.
    ///
    /// @return a string representing the expression
    String expression();

    String getContextKey();

    void setContextKey(String key);
}
