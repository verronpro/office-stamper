package pro.verron.officestamper.api;

/// Represents a comment processor for handling context-specific processing and operations
/// on comments, paragraphs, and runs within a document.
/// This interface serves as a contract
/// for implementing custom comment processors.
public interface CommentProcessor {

    /// Sets the processing context for the comment processor.
    /// This method serves to pass relevant contextual information, such as the
    /// current paragraph, run, comment, and placeholder being processed.
    /// It's always invoked before any custom methods of the custom
    /// [CommentProcessor] interface.
    ///
    /// @param processorContext the context in which the processor operates,
    ///                                                                         containing details about the paragraph,
    ///                         run,
    ///                                                                         comment, and placeholder being
    ///                         processed.
    void setProcessorContext(ProcessorContext processorContext);

    /// Finalizes the processing of a [DocxPart] document and commits any changes made to it.
    /// This method is used to ensure that all modifications performed during the processing
    /// of comments or other operations in the DocxPart are applied to the underlying document.
    ///
    /// @param docxPart the [DocxPart] instance representing a part of the document
    ///                                                 that is being processed; contains the underlying
    ///                 WordprocessingMLPackage
    ///                                                 document to which the changes are committed
    void commitChanges(DocxPart docxPart);

    /// Retrieves the current paragraph being processed.
    ///
    /// @return the current `Paragraph` object associated with the comment processor
    Paragraph getParagraph();

    /// Sets the current comment being processed in the comment processor.
    /// This method is typically invoked to specify the comment object
    /// associated with the current processing context.
    ///
    /// @param comment the comment object that is currently being processed
    void setCurrentCommentWrapper(Comment comment);

    /// Resets the internal state of the comment processor to its initial state.
    /// This method is intended to clear any stored context or settings,
    /// allowing the processor to be reused for a new processing task.
    void reset();
}
