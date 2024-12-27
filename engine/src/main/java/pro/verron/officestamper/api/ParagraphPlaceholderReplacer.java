package pro.verron.officestamper.api;


/// The ParagraphPlaceholderReplacer interface represents an object that can resolve expressions in a paragraph
/// and replace them with values provided by an expression resolver.
public interface ParagraphPlaceholderReplacer {

    /// Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
    ///
    ///
    /// @param docxPart  the docxPart in which to replace all expressions
    /// @param paragraph the paragraph in which to replace expressions
    /// @param context   the context root
    void resolveExpressionsForParagraph(DocxPart docxPart, Paragraph paragraph, Object context);
}
