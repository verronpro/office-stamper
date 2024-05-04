package pro.verron.docxstamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R.CommentReference;
import org.wickedsource.docxstamper.util.DocumentUtil;
import pro.verron.docxstamper.api.Comment;
import pro.verron.docxstamper.api.OfficeStamperException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public StandardComment(WordprocessingMLPackage document) {
        this.document = document;
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
        var parentElements = getParent().getContent();
        for (Object element : parentElements) {
            startFound = startFound || DocumentUtil.depthElementSearch(getCommentRangeStart(), element);
            if (startFound && !endFound) elements.add(element);
            endFound = endFound || DocumentUtil.depthElementSearch(getCommentRangeEnd(), element);
        }
        return elements;
    }

    /**
     * Creates a new document containing only the elements between the comment range anchors.
     *
     * @param document the document from which to copy the elements.
     *
     * @return a new document containing only the elements between the comment range anchors.
     *
     * @throws Exception if the sub template could not be created.
     * @deprecated use {@link CommentUtil#createSubWordDocument(Comment)} instead
     */
    @Deprecated(since = "1.6.8", forRemoval = true)
    @Override
    public WordprocessingMLPackage getSubTemplate(WordprocessingMLPackage document)
            throws Exception {
        return CommentUtil.createSubWordDocument(this);
    }

    /**
     * Creates a new document containing only the elements between the comment range anchors.
     * If the sub template could not be created, a
     * {@link OfficeStamperException} is thrown.
     *
     * @param document the document from which to copy the elements.
     *
     * @return a new document containing only the elements between the comment range anchors.
     * @deprecated use {@link CommentUtil#createSubWordDocument(Comment)} instead
     */
    @Deprecated(since = "1.6.8", forRemoval = true)
    @Override
    public WordprocessingMLPackage tryBuildingSubtemplate(
            WordprocessingMLPackage document
    ) {
        try {
            return getSubTemplate(document);
        } catch (Exception e) {
            throw new OfficeStamperException(e);
        }
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
