package pro.verron.officestamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import pro.verron.officestamper.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The DocxStamper class is an implementation of the {@link OfficeStamper}
 * interface that is used to stamp DOCX templates with a context object and
 * write the result to an output stream.
 *
 * @author Tom Hombergs
 * @author Joseph Verron
 * @version ${version}
 * @since 1.0.0
 */
public class DocxStamper
        implements OfficeStamper<WordprocessingMLPackage> {
    private final List<PreProcessor> preprocessors;
    private final PlaceholderReplacer placeholderReplacer;
    private final Function<DocxPart, CommentProcessorRegistry> commentProcessorRegistrySupplier;

    /**
     * Creates a new DocxStamper with the given configuration.
     *
     * @param configuration the configuration to use for this DocxStamper.
     */
    public DocxStamper(OfficeStamperConfiguration configuration) {
        this(configuration.isFailOnUnresolvedExpression(),
                configuration.isReplaceUnresolvedExpressions(),
                configuration.isLeaveEmptyOnExpressionError(),
                configuration.getUnresolvedExpressionsDefaultValue(),
                configuration.getLineBreakPlaceholder(),
                configuration.getEvaluationContextConfigurer(),
                configuration.getExpressionFunctions(),
                configuration.getResolvers(),
                configuration.getCommentProcessors(),
                configuration.getPreprocessors(),
                configuration.getSpelParserConfiguration());
    }

    private DocxStamper(
            boolean failOnUnresolvedExpression,
            boolean replaceUnresolvedExpressions,
            boolean leaveEmptyOnExpressionError,
            String unresolvedExpressionsDefaultValue,
            @NonNull String lineBreakPlaceholder,
            EvaluationContextConfigurer evaluationContextConfigurer,
            Map<Class<?>, Object> expressionFunctions,
            List<ObjectResolver> resolvers,
            Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> configurationCommentProcessors,
            List<PreProcessor> preprocessors,
            SpelParserConfiguration spelParserConfiguration
    ) {
        var commentProcessors = new HashMap<Class<?>, Object>();

        Function<ReflectiveOperationException, TypedValue> onResolutionFail = failOnUnresolvedExpression
                ? DocxStamper::throwException
                : exception -> new TypedValue(null);

        final StandardMethodResolver methodResolver = new StandardMethodResolver(commentProcessors,
                expressionFunctions,
                onResolutionFail);

        var evaluationContext = new StandardEvaluationContext();
        evaluationContextConfigurer.configureEvaluationContext(evaluationContext);
        evaluationContext.addMethodResolver(methodResolver);


        var expressionParser = new SpelExpressionParser(spelParserConfiguration);
        var expressionResolver = new ExpressionResolver(evaluationContext, expressionParser);

        var typeResolverRegistry = new ObjectResolverRegistry(resolvers);

        this.placeholderReplacer = new PlaceholderReplacer(typeResolverRegistry,
                expressionResolver,
                failOnUnresolvedExpression,
                replaceUnresolvedExpressions,
                unresolvedExpressionsDefaultValue,
                leaveEmptyOnExpressionError,
                Placeholders.raw(lineBreakPlaceholder));

        for (var entry : configurationCommentProcessors.entrySet()) {
            Class<?> aClass = entry.getKey();
            Function<ParagraphPlaceholderReplacer, CommentProcessor> processorFunction = entry.getValue();
            CommentProcessor value = processorFunction.apply(placeholderReplacer);
            commentProcessors.put(aClass, value);
        }


        this.commentProcessorRegistrySupplier = source -> new CommentProcessorRegistry(source,
                expressionResolver,
                commentProcessors,
                failOnUnresolvedExpression);

        this.preprocessors = preprocessors.stream()
                                          .toList();
    }

    private static TypedValue throwException(ReflectiveOperationException exception) {
        throw new OfficeStamperException("Error calling method", exception);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Reads in a .docx template and "stamps" it into the given OutputStream, using the specified context object to
     * fill out any expressions it finds.
     * </p>
     * <p>
     * In the .docx template you have the following options to influence the "stamping" process:
     * </p>
     * <ul>
     * <li>Use expressions like ${name} or ${person.isOlderThan(18)} in the template's text. These expressions are
     * resolved
     * against the contextRoot object you pass into this method and are replaced by the results.</li>
     * <li>Use comments within the .docx template to mark certain paragraphs to be manipulated. </li>
     * </ul>
     * <p>
     * Within comments, you can put expressions in which you can use the following methods by default:
     * </p>
     * <ul>
     * <li><em>displayParagraphIf(boolean)</em> to conditionally display paragraphs or not</li>
     * <li><em>displayTableRowIf(boolean)</em> to conditionally display table rows or not</li>
     * <li><em>displayTableIf(boolean)</em> to conditionally display whole tables or not</li>
     * <li><em>repeatTableRow(List&lt;Object&gt;)</em> to create a new table row for each object in the list and
     * resolve expressions
     * within the table cells against one of the objects within the list.</li>
     * </ul>
     * <p>
     * If you need a wider vocabulary of methods available in the comments, you can create your own ICommentProcessor
     * and register it via {@link OfficeStamperConfiguration#addCommentProcessor(Class, Function)}.
     * </p>
     */
    public void stamp(
            InputStream template, Object contextRoot, OutputStream out
    ) {
        try {
            WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
            stamp(document, contextRoot, out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * Same as {@link #stamp(InputStream, Object, OutputStream)} except that you
     * may pass in a DOCX4J document as a template instead
     * of an InputStream.
     */
    @Override public void stamp(
            WordprocessingMLPackage document, Object contextRoot, OutputStream out
    ) {
        try {
            var source = new DocxPart(document);
            preprocess(document);
            processComments(source, contextRoot);
            replaceExpressions(source, contextRoot);
            document.save(out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void preprocess(WordprocessingMLPackage document) {
        for (PreProcessor preprocessor : preprocessors) {
            preprocessor.process(document);
        }
    }

    private void processComments(
            DocxPart document,
            Object contextObject
    ) {
        document.getParts(Namespaces.HEADER)
                .forEach(header -> runProcessors(header, contextObject));

        runProcessors(document, contextObject);

        document.getParts(Namespaces.FOOTER)
                .forEach(footer -> runProcessors(footer, contextObject));
    }

    private void replaceExpressions(
            DocxPart document,
            Object contextObject
    ) {
        document.getParts(Namespaces.HEADER)
                .forEach(s -> placeholderReplacer.resolveExpressions(s, contextObject));
        placeholderReplacer.resolveExpressions(document, contextObject);
        document.getParts(Namespaces.FOOTER)
                .forEach(s -> placeholderReplacer.resolveExpressions(s, contextObject));
    }

    private void runProcessors(DocxPart source, Object contextObject) {
        var processors = commentProcessorRegistrySupplier.apply(source);
        processors.runProcessors(contextObject);
    }
}
