package pro.verron.officestamper.core;

import pro.verron.officestamper.api.CommentProcessorFactory;
import pro.verron.officestamper.api.CustomFunction;
import pro.verron.officestamper.api.EvaluationContextFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.SecurityMode;
import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.api.PostProcessor;
import pro.verron.officestamper.utils.svg.SvgUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// The [DocxStamper] class is an implementation of the [OfficeStamper] interface used to stamp DOCX templates with a
/// context object and write the result to an output stream.
///
/// @author Tom Hombergs
/// @author Joseph Verron
/// @since 1.0.0
public class DocxStamper
        implements OfficeStamper<OfficeDocument> {

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

    /// Reads in a document template and "stamps" it, using the specified context object to fill out any expressions it
    /// finds.
    ///
    /// In the template you have the following options to influence the "stamping" process:
    ///   - Use expressions like `${name}` or `${person.isOlderThan(18)}` in the template's text. These expressions are
    /// resolved against the contextRoot object you pass into this method and are replaced by the results.
    ///   - Use comments within the template to mark certain paragraphs to be manipulated.
    ///
    /// @param document the template to stamp
    /// @param contextRoot the context object to use for stamping
    /// @return the stamped document
    @Override
    public OfficeDocument stamp(OfficeDocument document, Object contextRoot) {
        preprocess(document);
        process(document, contextRoot);
        postprocess(document);
        return document;
    }

    private void preprocess(OfficeDocument document) {
        preprocessors.forEach(processor -> processor.process(document));
    }

    private void process(OfficeDocument document, Object contextRoot) {
        for (OfficePart part : document.parts()) {
            process(part, contextRoot);
        }
    }

    private void process(OfficePart part, Object contextRoot) {
        var contextTree = new ContextRoot(contextRoot);
        // Re-discover hooks after every mutation: processing rewrites the part content, so a
        // fixed snapshot would go stale (the legacy DocxIterator.reset() semantics).
        boolean processed;
        do {
            processed = false;
            for (DocxHook hook : DocxHook.ofHooks(part)) {
                var officeStamperContextFactory = new OfficeStamperEvaluationContextFactory(customFunctions,
                        commentProcessors,
                        interfaceFunctions,
                        contextFactory);
                if (hook.run(engineFactory, contextTree, officeStamperContextFactory)) {
                    processed = true;
                    break;
                }
            }
        } while (processed);
    }

    private void postprocess(OfficeDocument document) {
        postprocessors.forEach(processor -> processor.process(document));
    }
}
