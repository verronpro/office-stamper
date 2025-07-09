package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.springframework.lang.Nullable;

/**
 * Represents a comment processor for handling context-specific processing and operations
 * on comments, paragraphs, and runs within a document.
 * This interface serves as a contract
 * for implementing custom comment processors.
 */
public interface CommentProcessor {

    /**
     * Sets the processing context for the comment processor.
     * This method serves to pass relevant contextual information, such as the
     * current paragraph, run, comment, and placeholder being processed.
     * It's always invoked before any custom methods of the custom
     * {@link CommentProcessor} interface.
     *
     * @param processorContext the context in which the processor operates,
     *                         containing details about the paragraph, run,
     *                         comment, and placeholder being processed.
     */
    void setProcessorContext(ProcessorContext processorContext);

    /**
     * Sets the currently processed run in the comment processor.
     * This method should be called to specify the current run
     * within the context of processing a document.
     *
     * @param run the run object that is currently being processed,
     *            or null if there is no specific run to set
     */
    void setCurrentRun(@Nullable R run);

    /**
     * Finalizes the processing of a {@link DocxPart} document and commits any changes made to it.
     * This method is used to ensure that all modifications performed during the processing
     * of comments or other operations in the DocxPart are applied to the underlying document.
     *
     * @param docxPart the {@link DocxPart} instance representing a part of the document
     *                 that is being processed; contains the underlying WordprocessingMLPackage
     *                 document to which the changes are committed
     */
    default void commitChanges(DocxPart docxPart) {
        commitChanges(docxPart.document());
    }

    /**
     * Commits changes to the provided WordprocessingMLPackage document.
     * This method is deprecated and should not be used in new implementations.
     * It is retained only for compatibility with legacy implementations.
     *
     * @param document the WordprocessingMLPackage document to which changes were made
     * @throws OfficeStamperException always thrown, as this method should no longer be called
     * @deprecated since 2.3; for removal in future versions. Use updated methods or processes instead.
     */
    @Deprecated(since = "2.3", forRemoval = true) default void commitChanges(WordprocessingMLPackage document) {
        throw new OfficeStamperException("Should not be called since deprecation, only legacy implementations have a "
                                         + "reason to keep implementing this");
    }

    /**
     * Retrieves the current paragraph being processed.
     *
     * @return the current {@code Paragraph} object associated with the comment processor
     */
    Paragraph getParagraph();

    /**
     * Sets the current paragraph being processed in the comment processor.
     * This method is deprecated and scheduled for removal in a future version.
     *
     * @param paragraph the paragraph to set as the current paragraph being processed
     */
    @Deprecated(since = "2.6", forRemoval = true)
    void setParagraph(P paragraph);

    /**
     * Sets the current comment being processed in the comment processor.
     * This method is typically invoked to specify the comment object
     * associated with the current processing context.
     *
     * @param comment the comment object that is currently being processed
     */
    void setCurrentCommentWrapper(Comment comment);

    /**
     * Resets the internal state of the comment processor to its initial state.
     * This method is intended to clear any stored context or settings,
     * allowing the processor to be reused for a new processing task.
     */
    void reset();
}
