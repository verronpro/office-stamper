package pro.verron.officestamper.core;

import org.docx4j.wml.R;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlFactory;

/// Replaces expressions in a document with the values provided by the [ExpressionResolver].
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class PlaceholderReplacer
        implements ParagraphPlaceholderReplacer {

    private final ExpressionResolver resolver;
    private final ObjectResolverRegistry registry;
    private final ExceptionResolver exceptionResolver;

    /// Constructor for PlaceholderReplacer.
    ///
    /// @param registry the registry containing all available type resolvers.
    /// @param resolver the expression resolver used to resolve expressions in the document.
    public PlaceholderReplacer(
            ObjectResolverRegistry registry,
            ExpressionResolver resolver,
            ExceptionResolver exceptionResolver
    ) {
        this.registry = registry;
        this.resolver = resolver;
        this.exceptionResolver = exceptionResolver;
    }

    /// Finds expressions in a document and resolves them against the specified context object.
    /// The resolved values will then replace the expressions in the document.
    ///
    /// @param expressionContext the context root
    public void resolveExpressions(DocxPart document, Object expressionContext) {
        document.streamParagraphs()
                .forEach(paragraph -> resolveExpressionsForParagraph(document, paragraph, expressionContext));
    }

    /// Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
    ///
    /// @param docxPart  the document in which to replace all expressions.
    /// @param paragraph the paragraph in which to replace expressions.
    /// @param context   the context root
    @Override
    public void resolveExpressionsForParagraph(
            DocxPart docxPart,
            Paragraph paragraph,
            Object context
    ) {
        var expressions = Placeholders.findVariables(paragraph);
        for (var expression : expressions) {
            var replacement = resolve(docxPart, context, expression);
            paragraph.replace(expression, replacement);
        }
    }

    public R resolve(DocxPart docxPart, Object context, Placeholder placeholder) {
        var resolution = getResolution(context, placeholder);
        var expression = placeholder.expression();
        var contextClass = context.getClass();
        var contextSimpleName = contextClass.getSimpleName();
        var errorMessage = "Expression %s could not be resolved against context of type %s".formatted(expression,
                contextSimpleName);
        return resolve(docxPart, placeholder, resolution, errorMessage);
    }

    public @Nullable Object getResolution(Object context, Placeholder placeholder) {
        resolver.setContext(context);
        return resolver.resolve(placeholder);
    }

    public R resolve(DocxPart docxPart, Placeholder placeholder, @Nullable Object resolution, String errorMessage) {
        try {
            return registry.resolve(docxPart, placeholder, resolution);
        } catch (SpelEvaluationException | SpelParseException | OfficeStamperException e) {
            return WmlFactory.newRun(exceptionResolver.resolve(placeholder, errorMessage, e));
        }
    }
}
