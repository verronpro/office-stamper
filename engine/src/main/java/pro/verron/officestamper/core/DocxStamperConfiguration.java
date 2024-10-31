package pro.verron.officestamper.core;


import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.lang.NonNull;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.api.CustomFunction.NeedsBiFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsFunctionImpl;
import pro.verron.officestamper.core.functions.BiFunctionBuilder;
import pro.verron.officestamper.core.functions.FunctionBuilder;
import pro.verron.officestamper.core.functions.TriFunctionBuilder;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.preset.EvaluationContextConfigurers;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.Resolvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The {@link DocxStamperConfiguration} class represents the configuration for
 * the {@link DocxStamper} class.
 * It provides methods to customize the behavior of the stamper.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.3
 */
public class DocxStamperConfiguration
        implements OfficeStamperConfiguration {
    private final Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> commentProcessors =
            new HashMap<>();
    private final List<ObjectResolver> resolvers = new ArrayList<>();
    private final Map<Class<?>, Object> expressionFunctions = new HashMap<>();
    private final List<PreProcessor> preprocessors = new ArrayList<>();
    private final List<CustomFunction> functions = new ArrayList<>();
    private String lineBreakPlaceholder = "\n";
    private EvaluationContextConfigurer evaluationContextConfigurer = EvaluationContextConfigurers.defaultConfigurer();
    private boolean failOnUnresolvedExpression = true;
    private boolean leaveEmptyOnExpressionError = false;
    private boolean replaceUnresolvedExpressions = false;
    private String unresolvedExpressionsDefaultValue = null;
    private SpelParserConfiguration spelParserConfiguration = new SpelParserConfiguration();
    private ExceptionResolver exceptionResolver = computeExceptionResolver();

    /**
     * Creates a new configuration with default values.
     */
    public DocxStamperConfiguration() {
        CommentProcessorFactory pf = new CommentProcessorFactory(this);
        commentProcessors.put(CommentProcessorFactory.IRepeatProcessor.class, pf::repeat);
        commentProcessors.put(CommentProcessorFactory.IParagraphRepeatProcessor.class, pf::repeatParagraph);
        commentProcessors.put(CommentProcessorFactory.IRepeatDocPartProcessor.class, pf::repeatDocPart);
        commentProcessors.put(CommentProcessorFactory.ITableResolver.class, pf::tableResolver);
        commentProcessors.put(CommentProcessorFactory.IDisplayIfProcessor.class, pf::displayIf);
        commentProcessors.put(CommentProcessorFactory.IReplaceWithProcessor.class, pf::replaceWith);

        resolvers.addAll(List.of(Resolvers.image(),
                Resolvers.legacyDate(),
                Resolvers.isoDate(),
                Resolvers.isoTime(),
                Resolvers.isoDateTime(),
                Resolvers.nullToEmpty(),
                Resolvers.fallback()));
    }

    /**
     * Resets all the comment processors in the configuration. This method clears the
     * map of comment processors, effectively removing all registered comment processors.
     * Comment processors are used to process comments within the document.
     */
    public void resetCommentProcessors() {
        this.commentProcessors.clear();
    }

    /**
     * Resets all the resolvers in the DocxStamperConfiguration object.
     * This method clears the list of resolvers, effectively removing all registered resolvers.
     * Resolvers are used to resolve objects during the stamping process.
     */
    public void resetResolvers() {
        this.resolvers.clear();
    }

    /**
     * <p>isFailOnUnresolvedExpression.</p>
     *
     * @return a boolean
     */
    @Deprecated(since = "2.5", forRemoval = true) @Override public boolean isFailOnUnresolvedExpression() {
        return failOnUnresolvedExpression;
    }

    /**
     * If set to true, stamper will throw an {@link OfficeStamperException}
     * if a variable expression or processor expression within the document or within the comments is encountered that
     * cannot be resolved. Is set to true by default.
     *
     * @param failOnUnresolvedExpression a boolean
     *
     * @return a {@link DocxStamperConfiguration} object
     */
    @Deprecated(since = "2.5", forRemoval = true) @Override
    public DocxStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
        this.failOnUnresolvedExpression = failOnUnresolvedExpression;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    private ExceptionResolver computeExceptionResolver() {
        if (failOnUnresolvedExpression) return ExceptionResolvers.throwing();
        if (replaceWithDefaultOnError()) return ExceptionResolvers.defaulting(replacementDefault());
        return ExceptionResolvers.passing();
    }

    private boolean replaceWithDefaultOnError() {
        return isLeaveEmptyOnExpressionError() || isReplaceUnresolvedExpressions();
    }

    private String replacementDefault() {
        return isLeaveEmptyOnExpressionError() ? "" : getUnresolvedExpressionsDefaultValue();
    }

    /**
     * <p>isLeaveEmptyOnExpressionError.</p>
     *
     * @return a boolean
     */
    @Override public boolean isLeaveEmptyOnExpressionError() {
        return leaveEmptyOnExpressionError;
    }

    /**
     * <p>isReplaceUnresolvedExpressions.</p>
     *
     * @return a boolean
     */
    @Override public boolean isReplaceUnresolvedExpressions() {
        return replaceUnresolvedExpressions;
    }

    /**
     * <p>Getter for the field <code>unresolvedExpressionsDefaultValue</code>.</p>
     *
     * @return a {@link String} object
     */
    @Override public String getUnresolvedExpressionsDefaultValue() {
        return unresolvedExpressionsDefaultValue;
    }

    /**
     * Indicates the default value to use for expressions that doesn't resolve.
     *
     * @param unresolvedExpressionsDefaultValue value to use instead for expression that doesn't resolve
     *
     * @return a {@link DocxStamperConfiguration} object
     *
     * @see DocxStamperConfiguration#replaceUnresolvedExpressions
     */
    @Deprecated(since = "2.5", forRemoval = true) @Override
    public DocxStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue) {
        this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    /**
     * Indicates if a default value should replace expressions that don't resolve.
     *
     * @param replaceUnresolvedExpressions true to replace null value expression with resolved value (which is null),
     *                                     false to leave the expression as is
     *
     * @return a {@link DocxStamperConfiguration} object
     */
    @Deprecated(since = "2.5", forRemoval = true) @Override
    public DocxStamperConfiguration replaceUnresolvedExpressions(boolean replaceUnresolvedExpressions) {
        this.replaceUnresolvedExpressions = replaceUnresolvedExpressions;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    /**
     * If an error is caught while evaluating an expression, the expression will be replaced with an empty string
     * instead
     * of leaving the original expression in the document.
     *
     * @param leaveEmpty true to replace expressions with empty string when an error is caught while evaluating
     *
     * @return a {@link DocxStamperConfiguration} object
     */
    @Deprecated(since = "2.5", forRemoval = true) @Override public DocxStamperConfiguration leaveEmptyOnExpressionError(
            boolean leaveEmpty
    ) {
        this.leaveEmptyOnExpressionError = leaveEmpty;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    /**
     * Exposes all methods of a given interface to the expression language.
     *
     * @param interfaceClass the interface whose methods should be exposed in the expression language.
     * @param implementation the implementation that should be called to evaluate invocations of the interface methods
     *                       within the expression language. Must implement the interface above.
     *
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override public DocxStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass, Object implementation
    ) {
        this.expressionFunctions.put(interfaceClass, implementation);
        return this;
    }

    /**
     * Registers the specified ICommentProcessor as an implementation of the
     * specified interface.
     *
     * @param interfaceClass          the Interface which is implemented by the commentProcessor.
     * @param commentProcessorFactory the commentProcessor factory generating the specified interface.
     *
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override public DocxStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass, Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    ) {
        this.commentProcessors.put(interfaceClass, commentProcessorFactory);
        return this;
    }

    /**
     * Adds a preprocessor to the configuration.
     *
     * @param preprocessor the preprocessor to add.
     */
    @Override public void addPreprocessor(PreProcessor preprocessor) {
        preprocessors.add(preprocessor);
    }

    /**
     * <p>Getter for the field <code>lineBreakPlaceholder</code>.</p>
     *
     * @return a {@link String} object
     */
    @Override public String getLineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    /**
     * The String provided as lineBreakPlaceholder will be replaced with a line break
     * when stamping a document. If no lineBreakPlaceholder is provided, no replacement
     * will take place.
     *
     * @param lineBreakPlaceholder the String that should be replaced with line breaks during stamping.
     *
     * @return the configuration object for chaining.
     */
    @Override public DocxStamperConfiguration setLineBreakPlaceholder(@NonNull String lineBreakPlaceholder) {
        this.lineBreakPlaceholder = lineBreakPlaceholder;
        return this;
    }

    /**
     * <p>Getter for the field <code>evaluationContextConfigurer</code>.</p>
     *
     * @return a {@link EvaluationContextConfigurer} object
     */
    @Override public EvaluationContextConfigurer getEvaluationContextConfigurer() {
        return evaluationContextConfigurer;
    }

    /**
     * Provides an {@link EvaluationContextConfigurer} which may change the configuration of a Spring
     * {@link EvaluationContext} which is used for evaluating expressions
     * in comments and text.
     *
     * @param evaluationContextConfigurer the configurer to use.
     *
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override public DocxStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    ) {
        this.evaluationContextConfigurer = evaluationContextConfigurer;
        return this;
    }

    /**
     * <p>Getter for the field <code>spelParserConfiguration</code>.</p>
     *
     * @return a {@link SpelParserConfiguration} object
     */
    @Override public SpelParserConfiguration getSpelParserConfiguration() {
        return spelParserConfiguration;
    }

    /**
     * Sets the {@link SpelParserConfiguration} to use for expression parsing.
     * <p>
     * Note that this configuration will be used for all expressions in the document, including expressions in comments!
     * </p>
     *
     * @param spelParserConfiguration the configuration to use.
     *
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override public DocxStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    ) {
        this.spelParserConfiguration = spelParserConfiguration;
        return this;
    }

    /**
     * <p>Getter for the field <code>expressionFunctions</code>.</p>
     *
     * @return a {@link Map} object
     */
    @Override public Map<Class<?>, Object> getExpressionFunctions() {
        return expressionFunctions;
    }

    /**
     * <p>Getter for the field <code>commentProcessors</code>.</p>
     *
     * @return a {@link Map} object
     */
    @Override public Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors() {
        return commentProcessors;
    }

    /**
     * <p>Getter for the field <code>preprocessors</code>.</p>
     *
     * @return a {@link List} object
     */
    @Override public List<PreProcessor> getPreprocessors() {
        return preprocessors;
    }

    /**
     * Retrieves the list of resolvers.
     *
     * @return The list of object resolvers.
     */
    @Override public List<ObjectResolver> getResolvers() {
        return resolvers;
    }

    /**
     * Sets the resolvers for resolving objects in the DocxStamperConfiguration.
     * <p>
     * This method is the evolution of the method {@code addTypeResolver},
     * and the order in which the resolvers are ordered is determinant - the first resolvers
     * in the list will be tried first. If a fallback resolver is desired, it should be placed last in the list.
     *
     * @param resolvers The list of ObjectResolvers to be set.
     *
     * @return The updated DocxStamperConfiguration instance.
     */
    @Override public DocxStamperConfiguration setResolvers(
            List<ObjectResolver> resolvers
    ) {
        this.resolvers.clear();
        this.resolvers.addAll(resolvers);
        return this;
    }

    /**
     * Adds a resolver to the list of resolvers in the `DocxStamperConfiguration` object.
     * Resolvers are used to resolve objects during the stamping process.
     *
     * @param resolver The resolver to be added. This resolver should implement the `ObjectResolver` interface.
     *
     * @return The modified `DocxStamperConfiguration` object, with the resolver added to the beginning of the
     * resolver list.
     */
    @Override public DocxStamperConfiguration addResolver(ObjectResolver resolver) {
        resolvers.addFirst(resolver);
        return this;
    }

    @Override public ExceptionResolver getExceptionResolver() {
        return exceptionResolver;
    }

    @Override public DocxStamperConfiguration setExceptionResolver(ExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
        return this;
    }

    public void addCustomFunction(CustomFunction function) {
        this.functions.add(function);
    }

    @Override public List<CustomFunction> customFunctions() {
        return functions;
    }


    @Override public void addCustomFunction(String name, Supplier<?> implementation) {
        this.addCustomFunction(new CustomFunction(name, List.of(), args -> implementation.get()));
    }


    @Override public <T> NeedsFunctionImpl<T> addCustomFunction(String name, Class<T> class0) {
        return new FunctionBuilder<>(this, name, class0);
    }

    @Override public <T, U> NeedsBiFunctionImpl<T, U> addCustomFunction(String name, Class<T> class0, Class<U> class1) {
        return new BiFunctionBuilder<>(this, name, class0, class1);
    }

    @Override public <T, U, V> CustomFunction.NeedsTriFunctionImpl<T, U, V> addCustomFunction(
            String name,
            Class<T> class0, Class<U> class1, Class<V> class2
    ) {
        return new TriFunctionBuilder<>(this, name, class0, class1,class2);
    }
}
