package pro.verron.officestamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.springframework.expression.ExpressionParser;
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
    private final Function<DocxPart, ProcessorRegistry> processorRegistrySupplier;

    /// Creates a new DocxStamper with the given configuration.
    ///
    /// @param configuration the configuration to use for this DocxStamper.
    public DocxStamper(OfficeStamperConfiguration configuration) {
        this(configuration.getEvaluationContextConfigurer(),
                configuration.getExpressionFunctions(),
                configuration.customFunctions(),
                configuration.getResolvers(),
                configuration.getProcessors(),
                configuration.getPreprocessors(),
                configuration.getPostprocessors(),
                configuration.getExceptionResolver(),
                configuration.getExpressionParser());
    }

    private DocxStamper(
            EvaluationContextConfigurer evaluationContextConfigurer,
            Map<Class<?>, Object> expressionFunctions,
            List<CustomFunction> functions,
            List<ObjectResolver> resolvers,
            Map<Class<?>, Function<ParagraphPlaceholderReplacer, Processor>> processorSuppliers,
            List<PreProcessor> preprocessors,
            List<PostProcessor> postprocessors,
            ExceptionResolver exceptionResolver,
            ExpressionParser expressionParser
    ) {
        var evaluationContext = new StandardEvaluationContext();
        evaluationContextConfigurer.configureEvaluationContext(evaluationContext);

        var expressionResolver = new ExpressionResolver(evaluationContext, expressionParser);
        var objectResolverRegistry = new ObjectResolverRegistry(resolvers, exceptionResolver);
        var placeholderReplacer = new PlaceholderReplacer(objectResolverRegistry, expressionResolver);

        var processors = buildProcessors(processorSuppliers, placeholderReplacer);
        evaluationContext.addMethodResolver(new Invokers(streamInvokers(processors)));
        evaluationContext.addMethodResolver(new Invokers(streamInvokers(expressionFunctions)));
        evaluationContext.addMethodResolver(new Invokers(functions.stream()
                                                                  .map(Invokers::ofCustomFunction)));

        this.processorRegistrySupplier = source -> new ProcessorRegistry(source,
                expressionResolver,
                processors,
                exceptionResolver,
                objectResolverRegistry);

        this.preprocessors = new ArrayList<>(preprocessors);
        this.postprocessors = new ArrayList<>(postprocessors);
    }

    private Processors buildProcessors(
            Map<Class<?>, Function<ParagraphPlaceholderReplacer, Processor>> processorSuppliers,
            PlaceholderReplacer placeholderReplacer
    ) {
        var processors = new HashMap<Class<?>, Processor>();
        for (var entry : processorSuppliers.entrySet()) {
            processors.put(entry.getKey(),
                    entry.getValue()
                         .apply(placeholderReplacer));
        }
        return new Processors(processors);
    }

    public static void stamp(
            DocxStamperConfiguration configuration,
            WordprocessingMLPackage template,
            Object context,
            OutputStream output
    ) {
        new DocxStamper(configuration).stamp(template, context, output);
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
    /// If you need a wider vocabulary of methods available in the comments, you can create your own [Processor]
    /// and register it via [OfficeStamperConfiguration#addProcessor(Class, Function)].
    public void stamp(InputStream template, Object contextRoot, OutputStream out) {
        try {
            WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
            stamp(document, contextRoot, out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void preprocess(WordprocessingMLPackage document) {
        preprocessors.forEach(processor -> processor.process(document));
    }

    /// Same as [#stamp(InputStream, Object, OutputStream)] except that you
    /// may pass in a DOCX4J document as a template instead of an InputStream.
    @Override
    public void stamp(WordprocessingMLPackage document, Object contextRoot, OutputStream out) {
        try {
            var source = new TextualDocxPart(document);
            preprocess(document);
            process(source, contextRoot);
            postprocess(document);
            document.save(out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void postprocess(WordprocessingMLPackage document) {
        postprocessors.forEach(processor -> processor.process(document));
    }

    private void process(DocxPart document, Object contextObject) {
        document.streamParts(Namespaces.HEADER)
                .forEach(header -> runProcessors(contextObject, processorRegistrySupplier.apply(header)));
        runProcessors(contextObject, processorRegistrySupplier.apply(document));
        document.streamParts(Namespaces.FOOTER)
                .forEach(footer -> runProcessors(contextObject, processorRegistrySupplier.apply(footer)));
    }

    private void runProcessors(Object contextObject, ProcessorRegistry processorRegistry) {
        processorRegistry.runProcessors(contextObject);
    }
}
