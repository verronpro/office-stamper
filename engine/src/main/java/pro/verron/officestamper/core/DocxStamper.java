package pro.verron.officestamper.core;

import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.svg.SvgUtils;
import pro.verron.officestamper.utils.wml.DocxDocument;
import pro.verron.officestamper.utils.wml.DocxDocument.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// The [DocxStamper] class is an implementation of the [OfficeStamper] interface used to stamp DOCX templates with a
/// context object and write the result to an output stream.
///
/// @author Tom Hombergs
/// @author Joseph Verron
/// @since 1.0.0
public class DocxStamper implements OfficeStamper<DocxDocument> {

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
        this.contextFactory = configuration.getEvaluationContextFactory();
        this.interfaceFunctions = configuration.getExpressionFunctions();
        this.customFunctions = configuration.customFunctions();
        this.commentProcessors = configuration.getCommentProcessors();
        // Apply global SVG safe-mode preference early so that any SVG manipulations during stamping honor it.
        if (SecurityMode.PERMISSIVE.equals(configuration.getSvgSecurityMode())) SvgUtils.disableSafeMode();
        else SvgUtils.enableSafeMode();
        this.engineFactory = processorContext -> {
            var parserConfiguration = configuration.getParserConfiguration();
            var exceptionResolver = configuration.getExceptionResolver();
            var resolvers = configuration.getResolvers();
            var registry = new ObjectResolverRegistry(resolvers);
            var traceabilityReporter = configuration.getTraceabilityReporter();
            return new Engine(parserConfiguration, exceptionResolver, registry, processorContext, traceabilityReporter);
        };
        this.preprocessors = new ArrayList<>(configuration.getPreprocessors());
        this.postprocessors = new ArrayList<>(configuration.getPostprocessors());
    }

    /// Reads in a .docx template and "stamps" it, using the specified context object to fill out any expressions it
    /// finds.
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
    /// and register it via [OfficeStamperConfiguration#addCommentProcessor(Class, CommentProcessorFactory)].
    ///
    /// @param document    the .docx template to stamp
    /// @param contextRoot the context object to use for stamping
    /// @return the stamped document
    @Override
    public DocxDocument stamp(DocxDocument document, Object contextRoot) {
        preprocess(document);
        process(document, contextRoot);
        postprocess(document);
        return document;
    }

    private void preprocess(DocxDocument document) {
        preprocessors.forEach(processor -> processor.process(document));
    }

    private void process(DocxDocument document, Object contextRoot) {
        process(document.mainPart(), contextRoot);
        document.headerParts().forEach(part -> process(part, contextRoot));
        document.footerParts().forEach(part -> process(part, contextRoot));
    }

    private void postprocess(DocxDocument document) {
        postprocessors.forEach(processor -> processor.process(document));
    }

    private void process(Part part, Object contextRoot) {
        var contextTree = new ContextRoot(contextRoot);
        var iterator = DocxHook.ofHooks(part::content, part);
        while (iterator.hasNext()) {
            var hook = iterator.next();
            var officeStamperContextFactory = new OfficeStamperEvaluationContextFactory(customFunctions,
                    commentProcessors,
                    interfaceFunctions,
                    contextFactory
            );
            if (hook.run(engineFactory, contextTree, officeStamperContextFactory)) {
                iterator.reset();
            }
        }
    }

}
