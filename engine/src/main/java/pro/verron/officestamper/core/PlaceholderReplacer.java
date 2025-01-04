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

    /// Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
    ///
    /// @param docxPart  the document in which to replace all expressions.
    /// @param paragraph the paragraph in which to replace expressions.
    /// @param context   the context root
    @Override
    public void resolveExpressionsForParagraph(DocxPart docxPart, Paragraph paragraph, Object context) {
        resolver.setContext(context);

        var placeholders = Placeholders.findVariables(paragraph);
        for (var placeholder : placeholders) {
            var resolution = resolver.resolve(placeholder);
            var errMsg = computeErrMsg(context, placeholder);
            var replacement = resolve(docxPart, placeholder, resolution, errMsg);
            paragraph.replace(placeholder, replacement);
        }
    }

    private static String computeErrMsg(Object context, Placeholder placeholder) {
        var expression = placeholder.expression();
        var contextClass = context.getClass();
        var contextSimpleName = contextClass.getSimpleName();
        var errTemplate = "Expression %s could not be resolved against context of type %s";
        return errTemplate.formatted(expression, contextSimpleName);
    }

    public R resolve(DocxPart docxPart, Placeholder placeholder, @Nullable Object resolution, String errorMessage) {
        try {
            return registry.resolve(docxPart, placeholder, resolution);
        } catch (SpelEvaluationException | SpelParseException | OfficeStamperException e) {
            return WmlFactory.newRun(exceptionResolver.resolve(placeholder, errorMessage, e));
        }
    }
}
