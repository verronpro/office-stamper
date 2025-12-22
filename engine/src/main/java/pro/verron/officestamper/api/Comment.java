package pro.verron.officestamper.api;

import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// The Comment interface provides methods for managing comments in a document.
public interface Comment {


    /// Retrieves the paragraph associated with this comment.
    ///
    /// @return the [Paragraph] object associated with this comment
    Paragraph getParagraph();

    //TODO: Remove this visibility change

    /// Retrieves the CTSmartTagRun object associated with the start of this comment.
    ///
    /// @return the [CTSmartTagRun] object representing the start tag run of the comment
    CTSmartTagRun getStartTagRun();

    /// Retrieves the CommentRangeStart object associated with this comment.
    ///
    /// @return the [CommentRangeStart] object associated with this comment
    CommentRangeStart getCommentRangeStart();

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
    CommentRangeEnd getCommentRangeEnd();

    /// Retrieves the [CommentReference] object associated with this comment.
    ///
    /// @return the [CommentReference] object associated with this comment
    @Nullable CommentReference getCommentReference();

    /// Retrieves the comment associated with this object.
    ///
    /// @return the comment associated with this object
    Comments.Comment getComment();

    /// Retrieves the expression associated with the implementing entity.
    ///
    /// @return a string representing the expression
    String expression();
}
