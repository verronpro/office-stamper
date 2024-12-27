package pro.verron.officestamper.api;


import org.docx4j.wml.R;
import org.springframework.lang.Nullable;

/// CommentProcessor is an interface that defines the methods for processing comments in a .docx template.
public interface CommentProcessor {

    void setProcessorContext(ProcessorContext processorContext);

    /// Passes the run that is currently being processed (i.e., the run that is commented in the
    /// .docx template). This method is always called BEFORE the custom
    /// methods of the custom comment processor interface
    /// are called.
    ///
    /// @param run coordinates of the currently processed run within the template.
    void setCurrentRun(@Nullable R run);

    /// This method is called after all comments in the .docx template have been passed to the comment processor.
    /// All manipulations of the .docx document SHOULD BE done in this method.
    /// If certain manipulations are already done
    /// within the custom methods of a comment processor,
    /// the ongoing iteration over the paragraphs in the document
    /// may be disturbed.
    ///
    /// @param docxPart The DocxPart that can be manipulated by using the DOCX4J api.
    void commitChanges(DocxPart docxPart);

    Paragraph getParagraph();

    /// Passes the comment range wrapper that is currently being processed
    /// (i.e., the start and end of comment that in the .docx template).
    /// This method is always called BEFORE the custom methods of the custom comment
    /// processor interface are called.
    ///
    /// @param comment of the currently processed comment within the template.
    void setCurrentCommentWrapper(Comment comment);

    /// Resets all states in the comment processor so that it can be re-used in another stamping process.
    void reset();
}
