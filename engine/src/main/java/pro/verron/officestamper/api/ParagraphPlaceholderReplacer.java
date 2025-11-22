package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.core.Tag;

/// The ParagraphPlaceholderReplacer interface represents an object that can resolve expressions in a paragraph
/// and replace them with values provided by an expression resolver.
public interface ParagraphPlaceholderReplacer {

    /// Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
    ///
    /// Replace the previous [#resolveExpressionsForParagraph(Paragraph, Object, WordprocessingMLPackage)]
    ///
    /// @param docxPart  the docxPart in which to replace all expressions
    /// @param paragraph the paragraph in which to replace expressions
    /// @param context   the context root
    default void resolveExpressionsForParagraph(
            DocxPart docxPart,
            Tag tag,
            Object context
    ) {
        resolveExpressionsForParagraph(tag, context, docxPart.document());
    }

    /// Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
    ///
    /// @param paragraph the paragraph in which to replace expressions
    /// @param context   the context root
    /// @param document  the document in which to replace all expressions
    ///
    /// @deprecated replaced by [#resolveExpressionsForParagraph(DocxPart, Paragraph, Object)]
    @Deprecated(since = "2.3", forRemoval = true)
    void resolveExpressionsForParagraph(
            Tag tag,
            Object context,
            WordprocessingMLPackage document
    );
}
