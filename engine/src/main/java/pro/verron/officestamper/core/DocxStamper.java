package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.wml.ContentAccessor;
import org.springframework.expression.ExpressionParser;
import pro.verron.officestamper.api.*;

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
    private final EvaluationContextFactory contextFactory;
    private final Map<Class<?>, Object> interfaceFunctions;
    private final List<CustomFunction> customFunctions;
    private final Map<Class<?>, CommentProcessorFactory> commentProcessors;

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
            EvaluationContextFactory contextFactory,
            Map<Class<?>, Object> interfaceFunctions,
            List<CustomFunction> customFunctions,
            List<ObjectResolver> resolvers,
            Map<Class<?>, CommentProcessorFactory> commentProcessors,
            List<PreProcessor> preprocessors,
            List<PostProcessor> postprocessors,
            ExceptionResolver exceptionResolver,
            ExpressionParser expressionParser
    ) {
        this.contextFactory = contextFactory;
        this.interfaceFunctions = interfaceFunctions;
        this.customFunctions = customFunctions;
        this.commentProcessors = commentProcessors;
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
    ///
    /// @param document the .docx template to stamp
    /// @param contextRoot the context object to use for stamping
    ///
    /// @return the stamped document
    @Override
    public WordprocessingMLPackage stamp(WordprocessingMLPackage document, Object contextRoot) {
        preprocess(document);
        process(document, contextRoot);
        postprocess(document);
        return document;
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
        var iterator = Hook.ofHooks(part::content, part);
        while (iterator.hasNext()) {
            var hook = iterator.next();
            var officeStamperContextFactory = new OfficeStamperEvaluationContextFactory(customFunctions,
                    commentProcessors,
                    interfaceFunctions,
                    contextFactory);
            if (hook.run(engineFactory, contextTree, officeStamperContextFactory)) {
                iterator.reset();
            }
        }
    }

}
