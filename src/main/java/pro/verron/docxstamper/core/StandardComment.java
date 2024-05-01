package pro.verron.docxstamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.util.DocumentUtil;
import pro.verron.docxstamper.api.Comment;

import java.util.*;
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
    private Comments.Comment comment;
    private CommentRangeStart commentRangeStart;
    private CommentRangeEnd commentRangeEnd;
    private CommentReference commentReference;

    /**
     * <p>getParent.</p>
     *
     * @return the comment's author.
     */
    @Override
    public ContentAccessor getParent() {
        return findGreatestCommonParent(
                getCommentRangeEnd().getParent(),
                (ContentAccessor) getCommentRangeStart().getParent()
        );
    }

    /**
     * <p>getRepeatElements.</p>
     *
     * @return the elements in the document that are between the comment range anchors.
     */
    @Override
    public List<Object> getElements() {
        List<Object> repeatElements = new ArrayList<>();
        boolean startFound = false;
        for (Object element : getParent().getContent()) {
            if (!startFound && depthElementSearch(getCommentRangeStart(), element)) {
                startFound = true;
            }
            if (startFound) {
                repeatElements.add(element);
                if (depthElementSearch(getCommentRangeEnd(), element)) {
                    break;
                }
            }
        }
        return repeatElements;
    }

    /**
     * Creates a new document containing only the elements between the comment range anchors.
     *
     * @param document the document from which to copy the elements.
     *
     * @return a new document containing only the elements between the comment range anchors.
     *
     * @throws Exception if the sub template could not be created.
     */
    @Override
    public WordprocessingMLPackage getSubTemplate(WordprocessingMLPackage document)
            throws Exception {
        List<Object> repeatElements = getRepeatElements();

        WordprocessingMLPackage subDocument = WordprocessingMLPackage.createPackage();
        MainDocumentPart subDocumentMainDocumentPart = subDocument.getMainDocumentPart();

        CommentsPart commentsPart = new CommentsPart();
        subDocumentMainDocumentPart.addTargetPart(commentsPart);

        // copy the elements to repeat without comment range anchors
        List<Object> finalRepeatElements = repeatElements.stream()
                                                         .map(XmlUtils::deepCopy)
                                                         .collect(Collectors.toList());
        removeCommentAnchorsFromFinalElements(finalRepeatElements);
        subDocumentMainDocumentPart.getContent()
                                   .addAll(finalRepeatElements);

        // copy the images from parent document using the original repeat elements
        ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
        ContentAccessor fakeBody = wmlObjectFactory.createBody();
        fakeBody.getContent()
                .addAll(repeatElements);
        DocumentUtil.walkObjectsAndImportImages(fakeBody, document, subDocument);

        Comments comments = wmlObjectFactory.createComments();
        extractedSubComments(comments.getComment(), this.getChildren());
        commentsPart.setContents(comments);

        return subDocument;
    }

    /**
     * Creates a new document containing only the elements between the comment range anchors.
     * If the sub template could not be created, a
     * {@link DocxStamperException} is thrown.
     *
     * @param document the document from which to copy the elements.
     *
     * @return a new document containing only the elements between the comment range anchors.
     */
    @Override
    public WordprocessingMLPackage tryBuildingSubtemplate(
            WordprocessingMLPackage document
    ) {
        try {
            return getSubTemplate(document);
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

    private void removeCommentAnchorsFromFinalElements(List<Object> finalRepeatElements) {
        ContentAccessor fakeBody = () -> finalRepeatElements;
        CommentUtil.deleteCommentFromElement(fakeBody.getContent(), getComment().getId());
    }

    private void extractedSubComments(
            List<Comments.Comment> commentList,
            Set<Comment> commentChildren
    ) {
        Queue<Comment> q = new ArrayDeque<>(commentChildren);
        while (!q.isEmpty()) {
            Comment element = q.remove();
            commentList.add(element.getComment());
            if (element.getChildren() != null)
                q.addAll(element.getChildren());
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

    private ContentAccessor findGreatestCommonParent(Object end, ContentAccessor start) {
        if (depthElementSearch(end, start)) {
            return findInsertableParent(start);
        }
        return findGreatestCommonParent(end, (ContentAccessor) ((Child) start).getParent());
    }

    private boolean depthElementSearch(Object searchTarget, Object content) {
        content = XmlUtils.unwrap(content);
        if (searchTarget.equals(content)) {
            return true;
        }
        else if (content instanceof ContentAccessor contentAccessor) {
            for (Object object : contentAccessor.getContent()) {
                Object unwrappedObject = XmlUtils.unwrap(object);
                if (searchTarget.equals(unwrappedObject)
                        || depthElementSearch(searchTarget, unwrappedObject)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ContentAccessor findInsertableParent(ContentAccessor searchFrom) {
        if (!(searchFrom instanceof Tc || searchFrom instanceof Body)) {
            return findInsertableParent((ContentAccessor) ((Child) searchFrom).getParent());
        }
        return searchFrom;
    }

}
