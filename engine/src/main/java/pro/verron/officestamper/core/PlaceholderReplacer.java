package pro.verron.officestamper.core;

import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.Inserts;
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
    /// @param registry             the registry containing all available type resolvers.
    /// @param resolver             the expression resolver used to resolve expressions in the document.
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
        var tagIterator = DocxIterator.ofTags(document::content, "placeholder", document);
        while (tagIterator.hasNext()) {
            var tag = tagIterator.next();
            resolveExpressionsForParagraph(document, tag, expressionContext);
            tagIterator.reset();
        }
    }

    /// Resolves and replaces placeholder expressions within a specified paragraph tag.
    /// This method uses the provided context and document part to find and substitute
    /// placeholder expressions in the given tag with the corresponding resolved values.
    ///
    /// @param docxPart the part of the document containing the paragraph and associated content.
    /// @param tag      the tag representing the placeholder to be resolved and replaced.
    /// @param context  the context object that provides data for resolving the placeholder expressions.
    @Override
    public void resolveExpressionsForParagraph(DocxPart docxPart, Tag tag, Object context) {
        var replacement = resolve(docxPart, context, tag.asPlaceholder());
        tag.replace(replacement);
    }

    private Insert resolve(DocxPart docxPart, Object context, Placeholder placeholder) {
        try {
            resolver.setContext(context);
            var resolution = resolver.resolve(placeholder);
            return registry.resolve(docxPart, placeholder, resolution);
        } catch (SpelEvaluationException | SpelParseException | OfficeStamperException e) {
            var message =
                    "Expression %s could not be resolved against context of type %s".formatted(placeholder.expression(),
                    context.getClass()
                           .getSimpleName());
            var resolution = exceptionResolver.resolve(placeholder, message, e);
            return Inserts.of(WmlFactory.newRun(resolution));
        }
    }
}
