package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlFactory;

import static pro.verron.officestamper.utils.WmlFactory.newBr;

/**
 * Replaces expressions in a document with the values provided by the {@link ExpressionResolver}.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class PlaceholderReplacer
        implements ParagraphPlaceholderReplacer {

    private final ExpressionResolver resolver;
    private final ObjectResolverRegistry registry;
    private final Placeholder lineBreakPlaceholder;
    private final ExceptionResolver exceptionResolver;

    /**
     * <p>Constructor for PlaceholderReplacer.</p>
     *
     * @param registry             the registry containing all available type resolvers.
     * @param resolver             the expression resolver used to resolve expressions in the document.
     * @param linebreakPlaceholder if set to a non-null value,
     *                             all occurrences of this placeholder will be
     *                             replaced with a line break.
     */
    public PlaceholderReplacer(
            ObjectResolverRegistry registry,
            ExpressionResolver resolver,
            Placeholder linebreakPlaceholder,
            ExceptionResolver exceptionResolver
    ) {
        this.registry = registry;
        this.resolver = resolver;
        this.lineBreakPlaceholder = linebreakPlaceholder;
        this.exceptionResolver = exceptionResolver;
    }

    /**
     * Finds expressions in a document and resolves them against the specified context object.
     * The resolved values will then replace the expressions in the document.
     *
     * @param expressionContext the context root
     */
    public void resolveExpressions(DocxPart document, Object expressionContext) {
        document.streamParagraphs()
                .forEach(paragraph -> resolveExpressionsForParagraph(document, paragraph, expressionContext));
    }

    /**
     * Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
     *
     * @param docxPart  the document in which to replace all expressions.
     * @param paragraph the paragraph in which to replace expressions.
     * @param context   the context root
     */
    @Override public void resolveExpressionsForParagraph(
            DocxPart docxPart,
            Paragraph paragraph,
            Object context
    ) {
        var expressions = Placeholders.findVariables(paragraph);
        for (var expression : expressions) {
            var replacement = resolve(docxPart, context, expression);
            paragraph.replace(expression, replacement);
        }
        paragraph.replace(lineBreakPlaceholder, newBr());
    }

    private R resolve(DocxPart docxPart, Object context, Placeholder placeholder) {
        try {
            resolver.setContext(context);
            var resolution = resolver.resolve(placeholder);
            return registry.resolve(docxPart, placeholder, resolution);
        } catch (SpelEvaluationException
                 | SpelParseException
                 | OfficeStamperException e) {
            var message = "Expression %s could not be resolved against context of type %s"
                    .formatted(placeholder.expression(), context.getClass().getSimpleName());
            var resolution = exceptionResolver.resolve(placeholder, message, e);
            return WmlFactory.newRun(resolution);
        }
    }

    @Override
    public void resolveExpressionsForParagraph(Paragraph paragraph, Object context, WordprocessingMLPackage document) {
        throw new OfficeStamperException("Should not be called, since deprecated");
    }
}
