package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * The ParagraphPlaceholderReplacer interface represents an object that can resolve expressions in a paragraph
 * and replace them with values provided by an expression resolver.
 */
public interface ParagraphPlaceholderReplacer {

    /**
     * Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
     *
     * @param paragraph the paragraph in which to replace expressions
     * @param context   the context root
     * @param document  the document in which to replace all expressions
     */
    void resolveExpressionsForParagraph(
            Paragraph paragraph,
            Object context,
            WordprocessingMLPackage document
    );
}
