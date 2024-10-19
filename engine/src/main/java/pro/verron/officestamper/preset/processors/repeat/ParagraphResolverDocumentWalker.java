package pro.verron.officestamper.preset.processors.repeat;

import org.docx4j.wml.P;
import org.docx4j.wml.Tr;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.core.BaseDocumentWalker;
import pro.verron.officestamper.core.StandardParagraph;

/**
 * Walks through a document and replaces expressions with values from the given
 * expression context.
 * This walker only replaces expressions in paragraphs, not in tables.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.4.7
 */
class ParagraphResolverDocumentWalker
        extends BaseDocumentWalker {
    private final Object expressionContext;
    private final DocxPart docxPart;
    private final ParagraphPlaceholderReplacer placeholderReplacer;

    /**
     * <p>Constructor for ParagraphResolverDocumentWalker.</p>
     *
     * @param rowClone          The row to start with
     * @param expressionContext The context of the expressions to resolve
     * @param replacer          The placeholderReplacer to use for resolving
     */
    public ParagraphResolverDocumentWalker(
            DocxPart docxPart, Tr rowClone, Object expressionContext, ParagraphPlaceholderReplacer replacer
    ) {
        super(docxPart.from(rowClone));
        this.expressionContext = expressionContext;
        this.docxPart = docxPart;
        this.placeholderReplacer = replacer;
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void onParagraph(P paragraph) {
        var standardParagraph = StandardParagraph.from(docxPart, paragraph);
        placeholderReplacer.resolveExpressionsForParagraph(docxPart, standardParagraph, expressionContext);
    }
}
