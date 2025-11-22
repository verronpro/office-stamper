package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlFactory;

import static pro.verron.officestamper.utils.WmlFactory.newBr;

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
    private final Placeholder lineBreakPlaceholder;
    private final ExceptionResolver exceptionResolver;

    /// Constructor for PlaceholderReplacer.
    ///
    /// @param registry             the registry containing all available type resolvers.
    /// @param resolver             the expression resolver used to resolve expressions in the document.
    /// @param linebreakPlaceholder if set to a non-null value,
    ///                                                                                     all occurrences of this
    ///                             placeholder will be
    ///                                                                                     replaced with a line break.
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

    /// Finds expressions in a document and resolves them against the specified context object.
    /// The resolved values will then replace the expressions in the document.
    ///
    /// @param expressionContext the context root
    public void resolveExpressions(DocxPart document, Object expressionContext) {
        var tagIterator = DocxIterator.ofTags(document::content, "placeholder", document);
        while (tagIterator.hasNext()) {
            var tag = tagIterator.next();
            resolveExpressionsForParagraph(document, tag, expressionContext);
        }
    }

    /// Finds expressions in the given paragraph and replaces them with the values provided by the expression resolver.
    ///
    /// @param docxPart  the document in which to replace all expressions.
    /// @param paragraph the paragraph in which to replace expressions.
    /// @param context   the context root
    @Override
    public void resolveExpressionsForParagraph(DocxPart docxPart, Tag tag, Object context) {

        var replacement = resolve(docxPart, context, tag.asPlaceholder());
        tag.replace(replacement);
        tag.getParagraph().replace(lineBreakPlaceholder, newBr()); //TODO: move this line somewhere better
    }

    private R resolve(DocxPart docxPart, Object context, Placeholder placeholder) {
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
            return WmlFactory.newRun(resolution);
        }
    }

    /// Resolves expressions in the given paragraph using the specified context and document.
    /// This method is deprecated and should not be called. Calling it will result in an exception.
    ///
    /// @param paragraph the paragraph in which expressions were expected to be resolved
    /// @param context   the context object used for expression resolution
    /// @param document  the WordprocessingMLPackage document associated with the paragraph
    @Override
    public void resolveExpressionsForParagraph(Tag paragraph, Object context, WordprocessingMLPackage document) {
        throw new OfficeStamperException("Should not be called, since deprecated");
    }
}
