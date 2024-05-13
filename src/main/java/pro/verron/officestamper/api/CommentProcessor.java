package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

/**
 * CommentProcessor is an interface that defines the methods for processing comments in a .docx template.
 */
public interface CommentProcessor {
    /**
     * This method is called after all comments in the .docx template have been passed to the comment processor.
     * All manipulations of the .docx document SHOULD BE done in this method. If certain manipulations are already done
     * within the custom methods of a comment processor, the ongoing iteration over the paragraphs in the document
     * may be disturbed.
     *
     * @param document The Word document that can be manipulated by using the DOCX4J api.
     */
    void commitChanges(WordprocessingMLPackage document);

    /**
     * Passes the paragraph that is currently being processed (i.e., the paragraph that is commented in the
     * .docx template). This method is always called BEFORE the custom
     * methods of the custom comment processor interface
     * are called.
     *
     * @param paragraph coordinates of the currently processed paragraph within the template.
     */
    void setParagraph(P paragraph);

    /**
     * Passes the run that is currently being processed (i.e., the run that is commented in the
     * .docx template). This method is always called BEFORE the custom
     * methods of the custom comment processor interface
     * are called.
     *
     * @param run coordinates of the currently processed run within the template.
     */
    void setCurrentRun(R run);

    /**
     * Passes the comment range wrapper that is currently being processed
     * (i.e., the start and end of comment that in the .docx template).
     * This method is always called BEFORE the custom methods of the custom comment
     * processor interface are called.
     *
     * @param comment of the currently processed comment within the template.
     */
    void setCurrentCommentWrapper(Comment comment);

    /**
     * Passes the processed document, to make all linked data
     * (images, etc.) available
     * to processors that need it (example: repeatDocPart)
     *
     * @param document DocX template being processed.
     * @deprecated the document is passed to the processor through the commitChange method now,
     * and will probably pe passed through the constructor in the future
     */

    @Deprecated(since = "1.6.5", forRemoval = true)
    void setDocument(WordprocessingMLPackage document);

    /**
     * Resets all states in the comment processor so that it can be re-used in another stamping process.
     */
    void reset();
}
