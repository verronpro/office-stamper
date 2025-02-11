package pro.verron.officestamper.core;

import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.api.Placeholder;

/// Replaces expressions in a document with the values provided by the [ExpressionParser].
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class PlaceholderReplacer
        implements ParagraphPlaceholderReplacer {

    private final ExpressionParser resolver;
    private final ObjectResolverRegistry registry;

    /// Constructor for PlaceholderReplacer.
    ///
    /// @param registry the registry containing all available type resolvers.
    /// @param resolver the expression resolver used to resolve expressions in the document.
    public PlaceholderReplacer(ObjectResolverRegistry registry, ExpressionParser resolver) {
        this.registry = registry;
        this.resolver = resolver;
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
            var resolution = resolver.parse(placeholder);
            var errMsg = computeErrMsg(context, placeholder);
            var replacement = registry.resolve(docxPart, placeholder, resolution, errMsg);
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
}
