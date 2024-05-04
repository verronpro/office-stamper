package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.util.List;
import java.util.Set;

/**
 * The Comment interface provides methods for managing comments in a document.
 */
public interface Comment {

    /**
     * Retrieves the parent of the comment.
     *
     * @return the parent of the comment
     */
    ContentAccessor getParent();

    /**
     * Retrieves the elements in the document that are between the comment range anchors.
     *
     * @return a list of objects representing the elements between the comment range anchors.
     * @deprecated removed for replacement by {@link Comment#getElements()} which doesn't imply a specific usage
     */
    @Deprecated(since = "1.6.8", forRemoval = true)
    default List<Object> getRepeatElements() {
        return getElements();
    }

    /**
     * Retrieves the elements in the document that are between the comment range anchors.
     *
     * @return a list of objects representing the elements between the comment range anchors.
     */
    List<Object> getElements();

    /**
     * Creates a new document containing only the elements between the comment range anchors.
     *
     * @param document the document from which to copy the elements.
     *
     * @return a new document containing only the elements between the comment range anchors.
     *
     * @throws Exception if the sub template could not be created.
     */
    // TODO: Remove from this interface and move to an utility class
    WordprocessingMLPackage getSubTemplate(WordprocessingMLPackage document)
            throws Exception;

    /**
     * Tries to build a subtemplate from the given WordprocessingMLPackage document.
     *
     * @param document the source document from which to build the subtemplate
     *
     * @return the built subtemplate as a WordprocessingMLPackage
     */
    // TODO: Remove from this interface and move to an utility class
    WordprocessingMLPackage tryBuildingSubtemplate(WordprocessingMLPackage document);

    /**
     * Retrieves the {@link CommentRangeEnd} object associated with this comment.
     *
     * @return the {@link CommentRangeEnd} object associated with this comment
     */
    CommentRangeEnd getCommentRangeEnd();

    /**
     * Sets the {@link CommentRangeEnd} object associated with this comment.
     *
     * @param commentRangeEnd the {@link CommentRangeEnd} object to set
     */
    // TODO: Remove the setting method from interface to increase immutability
    void setCommentRangeEnd(CommentRangeEnd commentRangeEnd);

    /**
     * Retrieves the CommentRangeStart object associated with this comment.
     *
     * @return the CommentRangeStart object associated with this comment
     */
    CommentRangeStart getCommentRangeStart();

    /**
     * Sets the CommentRangeStart object associated with this comment.
     *
     * @param commentRangeStart the CommentRangeStart object to set
     */
    // TODO: Remove the setting method from interface to increase immutability
    void setCommentRangeStart(CommentRangeStart commentRangeStart);

    /**
     * Retrieves the {@link R.CommentReference} object associated with this comment.
     *
     * @return the {@link R.CommentReference} object associated with this comment
     */
    R.CommentReference getCommentReference();

    /**
     * Sets the comment reference for this comment.
     *
     * @param commentReference the comment reference to set
     */
    // TODO: Remove the setting method from interface to increase immutability
    void setCommentReference(R.CommentReference commentReference);

    /**
     * Retrieves the children of the comment.
     *
     * @return a set of Comment objects representing the children of the comment
     */
    Set<Comment> getChildren();

    /**
     * Sets the children of the comment.
     *
     * @param comments the set of Comment objects representing the children of the comment
     */
    // TODO: Remove the setting method from interface to increase immutability
    void setChildren(Set<Comment> comments);

    /**
     * Retrieves the comment associated with this object.
     *
     * @return the comment associated with this object
     */
    Comments.Comment getComment();

    /**
     * Sets the comment for this object.
     *
     * @param comment the comment to set
     */
    // TODO: Remove the setting method from interface to increase immutability
    void setComment(Comments.Comment comment);

    WordprocessingMLPackage getDocument();
}
