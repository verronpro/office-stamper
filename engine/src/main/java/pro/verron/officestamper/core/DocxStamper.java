package pro.verron.officestamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.springframework.expression.ExpressionParser;
import pro.verron.officestamper.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.docx4j.openpackaging.parts.relationships.Namespaces.FOOTER;
import static org.docx4j.openpackaging.parts.relationships.Namespaces.HEADER;

/// The [DocxStamper] class is an implementation of the [OfficeStamper] interface used to stamp DOCX templates with a
/// context object and write the result to an output stream.
///
/// @author Tom Hombergs
/// @author Joseph Verron
/// @version ${version}
/// @since 1.0.0
public class DocxStamper
        implements OfficeStamper<WordprocessingMLPackage> {

    private final List<PreProcessor> preprocessors;
    private final List<PostProcessor> postprocessors;
    private final EngineFactory engineFactory;
    private final EvaluationContextFactory evaluationContextFactory;
    private final Map<Class<?>, Object> expressionFunctions;
    private final List<CustomFunction> functions;
    private final Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors;

    /// Creates new [DocxStamper] with the given configuration.
    ///
    /// @param configuration the configuration to use for this [DocxStamper].
    public DocxStamper(OfficeStamperConfiguration configuration) {
        this(configuration.getEvaluationContextFactory(),
                configuration.getExpressionFunctions(),
                configuration.customFunctions(),
                configuration.getResolvers(),
                configuration.getCommentProcessors(),
                configuration.getPreprocessors(),
                configuration.getPostprocessors(),
                configuration.getExceptionResolver(),
                configuration.getExpressionParser());
    }

    private DocxStamper(
            EvaluationContextFactory evaluationContextFactory,
            Map<Class<?>, Object> expressionFunctions,
            List<CustomFunction> functions,
            List<ObjectResolver> resolvers,
            Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors,
            List<PreProcessor> preprocessors,
            List<PostProcessor> postprocessors,
            ExceptionResolver exceptionResolver,
            ExpressionParser expressionParser
    ) {
        this.evaluationContextFactory = evaluationContextFactory;
        this.expressionFunctions = expressionFunctions;
        this.functions = functions;
        this.configurationCommentProcessors = configurationCommentProcessors;
        engineFactory = computeEngine(resolvers, exceptionResolver, expressionParser);
        this.preprocessors = new ArrayList<>(preprocessors);
        this.postprocessors = new ArrayList<>(postprocessors);
    }

    private EngineFactory computeEngine(
            List<ObjectResolver> resolvers,
            ExceptionResolver exceptionResolver,
            ExpressionParser expressionParser
    ) {
        return processorContext -> {
            var typeResolverRegistry = new ObjectResolverRegistry(resolvers);
            return new Engine(expressionParser, exceptionResolver, typeResolverRegistry, processorContext);
        };
    }

    /// Reads in a .docx template and "stamps" it into the given OutputStream, using the specified context object to
    /// fill out any expressions it finds.
    ///
    /// In the .docx template you have the following options to influence the "stamping" process:
    ///   - Use expressions like `${name}` or `${person.isOlderThan(18)}` in the template's text. These expressions are
    /// resolved against the contextRoot object you pass into this method and are replaced by the results.
    ///   - Use comments within the .docx template to mark certain paragraphs to be manipulated.
    ///
    /// Within comments, you can put expressions in which you can use the following methods by default:
    ///   - `displayParagraphIf(boolean)` to conditionally display paragraphs or not
    ///   - `displayTableRowIf(boolean)` to conditionally display table rows or not
    ///   - `displayTableIf(boolean)` to conditionally display whole tables or not
    ///   - `repeatTableRow(List<Object>)` to create a new table row for each object in the list and resolve expressions
    /// within the table cells against one of the objects within the list.
    ///
    /// If you need a wider vocabulary of methods available in the comments, you can create your own [CommentProcessor]
    /// and register it via [OfficeStamperConfiguration#addCommentProcessor(Class, Function)].
    public void stamp(InputStream template, Object contextRoot, OutputStream out) {
        try {
            WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
            stamp(document, contextRoot, out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    /// Same as [#stamp(InputStream, Object, OutputStream)] except that you may pass in a DOCX4J document as a template
    /// instead of an InputStream.
    @Override
    public void stamp(WordprocessingMLPackage document, Object contextRoot, OutputStream out) {
        try {
            preprocess(document);
            process(document, contextRoot);
            postprocess(document);
            document.save(out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void preprocess(WordprocessingMLPackage document) {
        preprocessors.forEach(processor -> processor.process(document));
    }

    private void process(WordprocessingMLPackage document, Object contextRoot) {
        var mainDocumentPart = document.getMainDocumentPart();
        var mainPart = new TextualDocxPart(document, mainDocumentPart, mainDocumentPart);
        process(mainPart, contextRoot);

        var relationshipsPart = mainDocumentPart.getRelationshipsPart();
        for (var relationship : relationshipsPart.getRelationshipsByType(HEADER)) {
            Part part1 = relationshipsPart.getPart(relationship);
            TextualDocxPart textualDocxPart = new TextualDocxPart(document, part1, (ContentAccessor) part1);
            process(textualDocxPart, contextRoot);
        }

        for (var relationship : relationshipsPart.getRelationshipsByType(FOOTER)) {
            Part part = relationshipsPart.getPart(relationship);
            TextualDocxPart textualDocxPart = new TextualDocxPart(document, part, (ContentAccessor) part);
            process(textualDocxPart, contextRoot);
        }
    }

    private void postprocess(WordprocessingMLPackage document) {
        postprocessors.forEach(processor -> processor.process(document));
    }

    private void process(DocxPart part, Object contextRoot) {
        var contextTree = new ContextTree(contextRoot);
        var iterator = DocxIterator.ofHooks(part::content, part);
        while (iterator.hasNext()) {
            var hook = iterator.next();
            var officeStamperEvaluationContextFactory = computeEvaluationContext();
            if (hook.run(engineFactory, contextTree, officeStamperEvaluationContextFactory)) iterator.reset();
        }
    }

    private OfficeStamperEvaluationContextFactory computeEvaluationContext() {
        return new OfficeStamperEvaluationContextFactory(functions,
                configurationCommentProcessors,
                expressionFunctions,
                evaluationContextFactory);
    }
}
