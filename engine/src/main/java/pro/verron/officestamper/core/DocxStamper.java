package pro.verron.officestamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static pro.verron.officestamper.core.Invokers.streamInvokers;

/// The DocxStamper class is an implementation of the [OfficeStamper]
/// interface that is used to stamp DOCX templates with a context object and
/// write the result to an output stream.
///
/// @author Tom Hombergs
/// @author Joseph Verron
/// @version ${version}
/// @since 1.0.0
public class DocxStamper
        implements OfficeStamper<WordprocessingMLPackage> {

    private final List<PreProcessor> preprocessors;
    private final List<PostProcessor> postprocessors;
    private final Function<DocxPart, CommentProcessorRegistry> commentProcessorRegistrySupplier;

    /// Creates a new DocxStamper with the given configuration.
    ///
    /// @param configuration the configuration to use for this DocxStamper.
    public DocxStamper(OfficeStamperConfiguration configuration) {
        this(configuration.getEvaluationContextConfigurer(),
                configuration.getExpressionFunctions(),
                configuration.customFunctions(),
                configuration.getResolvers(),
                configuration.getCommentProcessors(),
                configuration.getPreprocessors(),
                configuration.getPostprocessors(),
                configuration.getSpelParserConfiguration(),
                configuration.getExceptionResolver());
    }

    private DocxStamper(
            EvaluationContextConfigurer evaluationContextConfigurer,
            Map<Class<?>, Object> expressionFunctions,
            List<CustomFunction> functions,
            List<ObjectResolver> resolvers,
            Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> configurationCommentProcessors,
            List<PreProcessor> preprocessors,
            List<PostProcessor> postprocessors,
            SpelParserConfiguration spelParserConfiguration,
            ExceptionResolver exceptionResolver
    ) {
        var expressionParser = new SpelExpressionParser(spelParserConfiguration);

        var evaluationContext = new StandardEvaluationContext();
        evaluationContextConfigurer.configureEvaluationContext(evaluationContext);

        var expressionResolver = new ExpressionResolver(evaluationContext, expressionParser);
        var typeResolverRegistry = new ObjectResolverRegistry(resolvers);
        var placeholderReplacer = new PlaceholderReplacer(typeResolverRegistry, expressionResolver, exceptionResolver);

        var commentProcessors = buildCommentProcessors(configurationCommentProcessors, placeholderReplacer);
        evaluationContext.addMethodResolver(new Invokers(streamInvokers(commentProcessors)));
        evaluationContext.addMethodResolver(new Invokers(streamInvokers(expressionFunctions)));
        evaluationContext.addMethodResolver(new Invokers(functions.stream()
                                                                  .map(Invokers::ofCustomFunction)));

        this.commentProcessorRegistrySupplier = source -> new CommentProcessorRegistry(source,
                expressionResolver,
                commentProcessors,
                exceptionResolver,
                placeholderReplacer);

        this.preprocessors = new ArrayList<>(preprocessors);
        this.postprocessors = new ArrayList<>(postprocessors);
    }

    public static void stamp(
            DocxStamperConfiguration configuration,
            WordprocessingMLPackage template,
            Object context,
            OutputStream output
    ) {
        new DocxStamper(configuration).stamp(template, context, output);
    }

    private CommentProcessors buildCommentProcessors(
            Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> commentProcessors,
            PlaceholderReplacer placeholderReplacer
    ) {
        var processors = new HashMap<Class<?>, CommentProcessor>();
        for (var entry : commentProcessors.entrySet()) {
            processors.put(entry.getKey(),
                    entry.getValue()
                         .apply(placeholderReplacer));
        }
        return new CommentProcessors(processors);
    }

    /// Reads in a .docx template and "stamps" it into the given OutputStream, using the specified context object to
    /// fill out any expressions it finds.
    ///
    /// In the .docx template you have the following options to influence the "stamping" process:
    ///   - Use expressions like ${name} or ${person.isOlderThan(18)} in the template's text. These expressions are
    ///     resolved
    ///     against the contextRoot object you pass into this method and are replaced by the results.
    ///   - Use comments within the .docx template to mark certain paragraphs to be manipulated.
    ///
    /// Within comments, you can put expressions in which you can use the following methods by default:
    ///   - _displayParagraphIf(boolean)_ to conditionally display paragraphs or not
    ///   - _displayTableRowIf(boolean)_ to conditionally display table rows or not
    ///   - _displayTableIf(boolean)_ to conditionally display whole tables or not
    ///   - _repeatTableRow(List&lt;Object&gt;)_ to create a new table row for each object in the list and
    ///     resolve expressions
    ///     within the table cells against one of the objects within the list.
    ///
    /// If you need a wider vocabulary of methods available in the comments, you can create your own ICommentProcessor
    /// and register it via [OfficeStamperConfiguration#addCommentProcessor(Class, Function)].
    public void stamp(InputStream template, Object contextRoot, OutputStream out) {
        try {
            WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
            stamp(document, contextRoot, out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    /// Same as [#stamp(InputStream, Object, OutputStream)] except that you
    /// may pass in a DOCX4J document as a template instead of an InputStream.
    @Override
    public void stamp(WordprocessingMLPackage document, Object contextRoot, OutputStream out) {
        try {
            var source = new TextualDocxPart(document);
            preprocess(document);
            processComments(source, contextRoot);
            postprocess(document);
            document.save(out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void preprocess(WordprocessingMLPackage document) {
        preprocessors.forEach(processor -> processor.process(document));
    }

    private void processComments(DocxPart document, Object contextObject) {
        document.streamParts(Namespaces.HEADER)
                .forEach(header -> runProcessors(header, contextObject));
        runProcessors(document, contextObject);
        document.streamParts(Namespaces.FOOTER)
                .forEach(footer -> runProcessors(footer, contextObject));
    }

    private void postprocess(WordprocessingMLPackage document) {
        postprocessors.forEach(processor -> processor.process(document));
    }

    private void runProcessors(DocxPart source, Object contextObject) {
        var processors = commentProcessorRegistrySupplier.apply(source);
        processors.runProcessors(contextObject);
    }
}
