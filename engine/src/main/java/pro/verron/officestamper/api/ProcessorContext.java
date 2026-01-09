package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.utils.iterator.ResetableIterator;
import pro.verron.officestamper.utils.wml.DocxIterator;

import java.util.Optional;

/// Represents a context within a document processing operation.
///
/// This class encapsulates key elements involved in the processing of a specific part of a
/// [WordprocessingMLPackage]-based document, such as a part of the document, a specific paragraph, an associated
/// comment, and an expression being evaluated or processed.
///
/// The [ProcessorContext] provides structured access to these elements, enabling seamless document traversal,
/// manipulation, and analysis during processing workflows.
public final class ProcessorContext {
    private final DocxPart part;
    private final Paragraph paragraph;
    private final Comment comment;
    private final String expression;
    private final ContextTree contextTree;

    /// Constructs a ProcessorContext.
    ///
    /// @param part The [DocxPart] representing a specific part of the document being processed.
    /// @param paragraph The [Paragraph] associated with the processing context.
    /// @param comment The [Comment] that is relevant to the current processing context.
    /// @param expression A [String] containing the expression or directive being evaluated.
    /// @param contextTree The [ContextTree] managing the hierarchical scopes for this context.
    public ProcessorContext(
            DocxPart part,
            Paragraph paragraph,
            Comment comment,
            String expression,
            ContextTree contextTree
    ) {
        this.part = part;
        this.paragraph = paragraph;
        this.comment = comment;
        this.expression = expression;
        this.contextTree = contextTree;
    }

    /// Returns an iterator over the content associated with the current comment's range.
    ///
    /// @return a [ResetableIterator] of [Object] representing the content within the comment's scope.
    public ResetableIterator<Object> contentIterator() {
        var parent = comment.getParent();
        var crs = comment.getCommentRangeStart();
        var cre = comment.getCommentRangeEnd();
        return new DocxIterator(parent).slice(crs, cre);
    }

    /// Returns the comment associated with the current processing context.
    ///
    /// @return the [Comment] object.
    public Comment comment() {return comment;}

    /// Returns the table row containing the current paragraph, if any.
    ///
    /// @return an [Optional] containing the [Table.Row], or empty if the paragraph is not in a table row.
    public Optional<Table.Row> tableRow() {
        return paragraph.parentTableRow();
    }

    /// Returns the table containing the current paragraph, if any.
    ///
    /// @return an [Optional] containing the [Table], or empty if the paragraph is not in a table.
    public Optional<Table> table() {
        return paragraph.parentTable();
    }

    /// Returns the document part currently being processed.
    ///
    /// @return the [DocxPart] object.
    public DocxPart part() {return part;}

    /// Returns the paragraph currently being processed.
    ///
    /// @return the [Paragraph] object.
    public Paragraph paragraph() {return paragraph;}

    /// Returns the expression or directive currently being evaluated.
    ///
    /// @return the expression as a [String].
    public String expression() {return expression;}

    /// Returns the context tree managing the hierarchical scopes for this context.
    ///
    /// @return the [ContextTree] object.
    public ContextTree contextHolder() {return contextTree;}
}
