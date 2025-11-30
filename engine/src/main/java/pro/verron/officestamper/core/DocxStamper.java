package pro.verron.officestamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static org.docx4j.openpackaging.parts.relationships.Namespaces.*;
import static pro.verron.officestamper.core.Invokers.streamInvokers;

/// The [DocxStamper] class is an implementation of the [OfficeStamper]
/// interface that is used to stamp DOCX templates with a context object and
/// write the result to an output stream.
///
/// @author Tom Hombergs
/// @author Joseph Verron
/// @version ${version}
/// @since 1.0.0
public class DocxStamper
        implements OfficeStamper<WordprocessingMLPackage> {

    private static final Logger log = LoggerFactory.getLogger(DocxStamper.class);
    private final List<PreProcessor> preprocessors;
    private final List<PostProcessor> postprocessors;
    private final EngineFactory engineFactory;

    /// Creates new [DocxStamper] with the given configuration.
    ///
    /// @param configuration the configuration to use for this [DocxStamper].
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
            Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors,
            List<PreProcessor> preprocessors,
            List<PostProcessor> postprocessors,
            SpelParserConfiguration spelParserConfiguration,
            ExceptionResolver exceptionResolver
    ) {
        var expressionParser = new SpelExpressionParser(spelParserConfiguration);

        engineFactory = computeEngine(evaluationContextConfigurer,
                expressionFunctions,
                functions,
                resolvers,
                configurationCommentProcessors,
                exceptionResolver,
                expressionParser);
        this.preprocessors = new ArrayList<>(preprocessors);
        this.postprocessors = new ArrayList<>(postprocessors);
    }

    private EngineFactory computeEngine(
            EvaluationContextConfigurer evaluationContextConfigurer,
            Map<Class<?>, Object> expressionFunctions,
            List<CustomFunction> functions,
            List<ObjectResolver> resolvers,
            Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors,
            ExceptionResolver exceptionResolver,
            SpelExpressionParser expressionParser
    ) {
        return processorContext -> {
            var evaluationContext = new StandardEvaluationContext();
            evaluationContextConfigurer.configureEvaluationContext(evaluationContext);

            var expressionResolver = new ExpressionResolver(evaluationContext, expressionParser);
            var typeResolverRegistry = new ObjectResolverRegistry(resolvers);
            var engine = new Engine(expressionResolver, exceptionResolver, typeResolverRegistry);

            var processors = instantiate(configurationCommentProcessors, processorContext, engine);
            var processorResolvers = new Invokers(streamInvokers(processors));
            evaluationContext.addMethodResolver(processorResolvers);
            evaluationContext.addMethodResolver(new Invokers(streamInvokers(expressionFunctions)));
            evaluationContext.addMethodResolver(new Invokers(functions));
            return engine;
        };
    }

    /// Returns a set view of the mappings contained in this map.
    /// Each entry in the set is a mapping between a `Class<?>` key and its associated
    /// `CommentProcessor` value.
    ///
    /// @return a set of map entries representing the associations between `Class<?>` keys
    ///         and their corresponding `CommentProcessor` values in this map

    private static Map<Class<?>, CommentProcessor> instantiate(
            Map<Class<?>, CommentProcessorFactory> processorFactoryMap,
            ProcessorContext processorContext,
            PlaceholderReplacer placeholderReplacer
    ) {
        Map<Class<?>, CommentProcessor> map = new HashMap<>();
        for (Map.Entry<Class<?>, CommentProcessorFactory> entry : processorFactoryMap.entrySet()) {
            var processorClass = entry.getKey();
            var processorFactory = entry.getValue();
            var processor = processorFactory.create(processorContext, placeholderReplacer);
            map.put(processorClass, processor);
        }
        return map;


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
            preprocess(document);
            process(new TextualDocxPart(document), contextRoot);
            postprocess(document);
            document.save(out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void preprocess(WordprocessingMLPackage document) {
        preprocessors.forEach(processor -> processor.process(document));
    }

    private void process(DocxDocument document, Object contextRoot) {
        document.process(part -> processPart(part, contextRoot));
    }

    private void postprocess(WordprocessingMLPackage document) {
        postprocessors.forEach(processor -> processor.process(document));
    }

    private void processPart(DocxPart part, Object contextRoot) {
        var type = part.type();
        switch (type) {
            case DOCUMENT, HEADER, FOOTER -> processTextualPart(part, contextRoot);
            default -> log.info("Unknown part type: {}", type);
        }

    }

    private void processTextualPart(DocxPart part, Object contextRoot) {
        var iterator = DocxIterator.ofHooks(part::content, part);
        while (iterator.hasNext()) {
            var hook = iterator.next();
            hook.ifPresent(h -> {if (h.run(engineFactory, contextRoot)) iterator.reset();});
        }
    }
}
