package pro.verron.officestamper.core;


import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.lang.NonNull;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.api.CustomFunction.NeedsBiFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsFunctionImpl;
import pro.verron.officestamper.core.functions.BiFunctionBuilder;
import pro.verron.officestamper.core.functions.FunctionBuilder;
import pro.verron.officestamper.core.functions.TriFunctionBuilder;
import pro.verron.officestamper.preset.EvaluationContextConfigurers;
import pro.verron.officestamper.preset.ExceptionResolvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/// The [DocxStamperConfiguration] class represents the configuration for the [DocxStamper] class.
/// It provides methods to customize the behavior of the stamper.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.3
public class DocxStamperConfiguration
        implements OfficeStamperConfiguration {
    private final Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> commentProcessors;
    private final List<ObjectResolver> resolvers;
    private final Map<Class<?>, Object> expressionFunctions;
    private final List<PreProcessor> preprocessors;
    private final List<PostProcessor> postprocessors;
    private final List<CustomFunction> functions;
    private String lineBreakPlaceholder;
    private EvaluationContextConfigurer evaluationContextConfigurer;
    private boolean failOnUnresolvedExpression;
    private boolean leaveEmptyOnExpressionError;
    private boolean replaceUnresolvedExpressions;
    private String unresolvedExpressionsDefaultValue;
    private SpelParserConfiguration spelParserConfiguration;
    private ExceptionResolver exceptionResolver;

    /**
     * Constructs a new instance of the {@code DocxStamperConfiguration} class
     * and initializes its default configuration settings.
     *
     * This constructor sets up internal structures and default behaviors
     * for managing document stamping configurations, including:
     * - Initializing collections for processors, resolvers, and functions.
     * - Setting default values for expression handling and evaluation.
     * - Creating and configuring a default {@code SpelParserConfiguration}.
     * - Establishing resolvers and exception handling strategies.
     */
    public DocxStamperConfiguration() {
        commentProcessors = new HashMap<>();
        resolvers = new ArrayList<>();
        expressionFunctions = new HashMap<>();
        preprocessors = new ArrayList<>();
        postprocessors = new ArrayList<>();
        functions = new ArrayList<>();
        evaluationContextConfigurer = EvaluationContextConfigurers.defaultConfigurer();
        lineBreakPlaceholder = "\n";
        failOnUnresolvedExpression = true;
        leaveEmptyOnExpressionError = false;
        replaceUnresolvedExpressions = false;
        unresolvedExpressionsDefaultValue = null;
        spelParserConfiguration = new SpelParserConfiguration();
        exceptionResolver = computeExceptionResolver();
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

    /// Resets all processors in the configuration.
    public void resetCommentProcessors() {
        this.commentProcessors.clear();
    }

    /// Resets all resolvers in the configuration.
    public void resetResolvers() {
        this.resolvers.clear();
    }

    @Deprecated(since = "2.5", forRemoval = true)
    @Override
    public boolean isFailOnUnresolvedExpression() {
        return failOnUnresolvedExpression;
    }

    /// If true, stamper throws an [OfficeStamperException] if an expression within the document can’t be resolved.
    /// Set to `TRUE` by default.
    ///
    /// @param failOnUnresolvedExpression a boolean
    ///
    /// @return the same [DocxStamperConfiguration] object
    @Deprecated(since = "2.5", forRemoval = true)
    @Override
    public DocxStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
        this.failOnUnresolvedExpression = failOnUnresolvedExpression;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    @Override
    public boolean isLeaveEmptyOnExpressionError() {
        return leaveEmptyOnExpressionError;
    }

    @Override
    public boolean isReplaceUnresolvedExpressions() {
        return replaceUnresolvedExpressions;
    }

    @Override
    public String getUnresolvedExpressionsDefaultValue() {
        return unresolvedExpressionsDefaultValue;
    }

    /// Default value to use for expressions that doesn't resolve.
    ///
    /// @param unresolvedExpressionsDefaultValue value to use instead for expression that doesn't resolve
    ///
    /// @return a [DocxStamperConfiguration] object
    ///
    /// @see DocxStamperConfiguration#replaceUnresolvedExpressions
    @Deprecated(since = "2.5", forRemoval = true)
    @Override
    public DocxStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue) {
        this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    /// Indicates if a default value should replace expressions that don't resolve.
    ///
    /// @param replaceUnresolvedExpressions true to replace expression with resolved value `null`
    ///
    ///
    ///                                                                         false to leave the expression as is.
    ///
    /// @return a [DocxStamperConfiguration] object
    @Deprecated(since = "2.5", forRemoval = true)
    @Override
    public DocxStamperConfiguration replaceUnresolvedExpressions(boolean replaceUnresolvedExpressions) {
        this.replaceUnresolvedExpressions = replaceUnresolvedExpressions;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    /// Indicate if expressions failing during evaluation needs removal.
    ///
    /// @param leaveEmpty true to replace expressions with empty string when an error occurs during evaluation.
    ///
    /// @return a [DocxStamperConfiguration] object
    @Deprecated(since = "2.5", forRemoval = true)
    @Override
    public DocxStamperConfiguration leaveEmptyOnExpressionError(
            boolean leaveEmpty
    ) {
        this.leaveEmptyOnExpressionError = leaveEmpty;
        this.exceptionResolver = computeExceptionResolver();
        return this;
    }

    /// Exposes all methods of a given interface to the expression language.
    ///
    /// @param interfaceClass the interface holding methods to expose in the expression language.
    /// @param implementation the implementation to call to evaluate invocations of those methods.
    ///
    ///                       Must implement the
    ///                                             mentioned interface.
    ///
    /// @return a [DocxStamperConfiguration] object
    @Override
    public DocxStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass,
            Object implementation
    ) {
        this.expressionFunctions.put(interfaceClass, implementation);
        return this;
    }

    /// Registers the specified ICommentProcessor as an implementation of the specified interface.
    ///
    /// @param interfaceClass          the interface, implemented by the commentProcessor.
    /// @param commentProcessorFactory the commentProcessor factory generating instances of the specified interface.
    ///
    /// @return a [DocxStamperConfiguration] object
    @Override
    public DocxStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    ) {
        this.commentProcessors.put(interfaceClass, commentProcessorFactory);
        return this;
    }

    /// Adds a preprocessor to the configuration.
    ///
    /// @param preprocessor the preprocessor to add.
    @Override
    public void addPreprocessor(PreProcessor preprocessor) {
        preprocessors.add(preprocessor);
    }


    @Override
    public String getLineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    /// String to replace with a line break when stamping a document.
    /// By default, `\\n` is the placeholder.
    ///
    /// @param lineBreakPlaceholder string to replace with line breaks during stamping.
    ///
    /// @return the configuration object for chaining.
    @Override
    public DocxStamperConfiguration setLineBreakPlaceholder(@NonNull String lineBreakPlaceholder) {
        this.lineBreakPlaceholder = lineBreakPlaceholder;
        return this;
    }

    @Override
    public EvaluationContextConfigurer getEvaluationContextConfigurer() {
        return evaluationContextConfigurer;
    }

    /// Provides an [EvaluationContextConfigurer] which may change the configuration of a Spring
    /// [EvaluationContext] used for evaluating expressions in comments and text.
    ///
    /// @param evaluationContextConfigurer the configurer to use.
    ///
    /// @return the configuration object for chaining.
    @Override
    public DocxStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    ) {
        this.evaluationContextConfigurer = evaluationContextConfigurer;
        return this;
    }

    @Override
    public SpelParserConfiguration getSpelParserConfiguration() {
        return spelParserConfiguration;
    }

    /// Sets the [SpelParserConfiguration] used for expression parsing.
    /// Note that this configuration is the same for all expressions in the document, including expressions in comments.
    ///
    /// @param spelParserConfiguration the configuration to use.
    ///
    /// @return the configuration object for chaining.
    @Override
    public DocxStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    ) {
        this.spelParserConfiguration = spelParserConfiguration;
        return this;
    }

    @Override
    public Map<Class<?>, Object> getExpressionFunctions() {
        return expressionFunctions;
    }

    @Override
    public Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors() {
        return commentProcessors;
    }

    @Override
    public List<PreProcessor> getPreprocessors() {
        return preprocessors;
    }

    @Override
    public List<ObjectResolver> getResolvers() {
        return resolvers;
    }

    /// Sets resolvers for resolving objects in the DocxStamperConfiguration.
    ///
    /// This method is the evolution of the method `addTypeResolver`,
    /// and the order in which the resolvers are ordered is determinant - the first resolvers
    /// in the list will be tried first. If a fallback resolver is desired, it should be placed last in the list.
    ///
    /// @param resolvers The list of ObjectResolvers to be set.
    ///
    /// @return the configuration object for chaining.
    @Override
    public DocxStamperConfiguration setResolvers(List<ObjectResolver> resolvers) {
        this.resolvers.clear();
        this.resolvers.addAll(resolvers);
        return this;
    }

    /// Adds a resolver to the list of resolvers in the `DocxStamperConfiguration` object.
    /// Resolvers are used to resolve objects during the stamping process.
    ///
    /// @param resolver The resolver to be added. This resolver should implement the `ObjectResolver` interface.
    ///
    /// @return The modified `DocxStamperConfiguration` object, with the resolver added to the beginning of the
    /// resolver list.
    @Override
    public DocxStamperConfiguration addResolver(ObjectResolver resolver) {
        resolvers.addFirst(resolver);
        return this;
    }

    @Override
    public ExceptionResolver getExceptionResolver() {
        return exceptionResolver;
    }

    @Override
    public DocxStamperConfiguration setExceptionResolver(ExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
        return this;
    }

    @Override
    public List<CustomFunction> customFunctions() {
        return functions;
    }

    @Override
    public void addCustomFunction(String name, Supplier<?> implementation) {
        this.addCustomFunction(new CustomFunction(name, List.of(), args -> implementation.get()));
    }

    public void addCustomFunction(CustomFunction function) {
        this.functions.add(function);
    }

    @Override
    public <T> NeedsFunctionImpl<T> addCustomFunction(String name, Class<T> class0) {
        return new FunctionBuilder<>(this, name, class0);
    }

    @Override
    public <T, U> NeedsBiFunctionImpl<T, U> addCustomFunction(String name, Class<T> class0, Class<U> class1) {
        return new BiFunctionBuilder<>(this, name, class0, class1);
    }

    @Override
    public <T, U, V> CustomFunction.NeedsTriFunctionImpl<T, U, V> addCustomFunction(
            String name,
            Class<T> class0,
            Class<U> class1,
            Class<V> class2
    ) {
        return new TriFunctionBuilder<>(this, name, class0, class1, class2);
    }

    @Override
    public List<PostProcessor> getPostprocessors() {
        return postprocessors;
    }

    @Override
    public void addPostprocessor(PostProcessor postprocessor) {
        postprocessors.add(postprocessor);
    }
}
