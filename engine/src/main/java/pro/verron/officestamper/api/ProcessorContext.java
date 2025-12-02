package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.core.ContextBranch;

/// Represents a context within a document processing operation.
///
/// This class encapsulates key elements involved in the processing of a specific part of a
/// [WordprocessingMLPackage]-based document, such as a part of the document, a specific paragraph, an associated
/// comment, and an expression being evaluated or processed.
///
/// The [ProcessorContext] provides structured access to these elements, enabling seamless document traversal,
/// manipulation, and analysis during processing workflows.
///
/// @param part The [DocxPart] representing a specific part of the document being processed.
/// @param paragraph The [Paragraph] associated with the processing context.
/// @param comment The [Comment] that is relevant to the current processing context.
/// @param expression A [String] containing the expression or directive being evaluated.
public record ProcessorContext(
        DocxPart part, Paragraph paragraph, Comment comment, String expression, ContextBranch branch
) {

}
