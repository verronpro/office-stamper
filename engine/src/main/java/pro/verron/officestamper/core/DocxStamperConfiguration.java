package pro.verron.officestamper.core;


import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.api.CustomFunction.NeedsBiFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsTriFunctionImpl;
import pro.verron.officestamper.core.functions.BiFunctionBuilder;
import pro.verron.officestamper.core.functions.FunctionBuilder;
import pro.verron.officestamper.core.functions.TriFunctionBuilder;
import pro.verron.officestamper.preset.EvaluationContextFactories;
import pro.verron.officestamper.preset.ExceptionResolvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/// The [DocxStamperConfiguration] class represents the configuration for the [DocxStamper] class.
///
/// It provides methods to customize the behavior of the stamper.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.3
public class DocxStamperConfiguration
        implements OfficeStamperConfiguration {
    private final Map<Class<?>, CommentProcessorFactory> commentProcessors;
    private final List<ObjectResolver> resolvers;
    private final Map<Class<?>, Object> expressionFunctions;
    private final List<PreProcessor> preprocessors;
    private final List<PostProcessor> postprocessors;
    private final List<CustomFunction> functions;
    private EvaluationContextFactory evaluationContextFactory;
    private ExpressionParser expressionParser;
    private ExceptionResolver exceptionResolver;

    /// Constructs a new instance of the [DocxStamperConfiguration] class and initializes its default configuration
    /// settings.
    ///
    /// This constructor sets up internal structures and default behaviors for managing document stamping
    /// configurations, including:
    /// - Initializing collections for processors, resolvers, and functions.
    /// - Setting default values for expression handling and evaluation.
    /// - Creating and configuring a default `SpelParserConfiguration`.
    /// - Establishing resolvers and exception handling strategies.
    public DocxStamperConfiguration() {
        commentProcessors = new HashMap<>();
        resolvers = new ArrayList<>();
        expressionFunctions = new HashMap<>();
        preprocessors = new ArrayList<>();
        postprocessors = new ArrayList<>();
        functions = new ArrayList<>();
        evaluationContextFactory = EvaluationContextFactories.defaultFactory();
        expressionParser = new SpelExpressionParser();
        exceptionResolver = ExceptionResolvers.throwing();
    }

    /// Exposes all methods of a given interface to the expression language.
    ///
    /// @param interfaceClass the interface holding methods to expose in the expression language.
    /// @param implementation the implementation to call to evaluate invocations of those methods, it must
    ///         implement the mentioned interface.
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

    /// Registers the specified [CommentProcessor] as an implementation of the specified interface.
    ///
    /// @param interfaceClass the interface, implemented by the commentProcessor.
    /// @param commentProcessorFactory the commentProcessor factory generating instances of the specified
    ///         interface.
    ///
    /// @return a [DocxStamperConfiguration] object
    @Override
    public DocxStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            CommentProcessorFactory commentProcessorFactory
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

    /// Retrieves the configured [EvaluationContextFactory] instance.
    ///
    /// @return an instance of [EvaluationContextFactory] used for creating evaluation contexts
    @Override
    public EvaluationContextFactory getEvaluationContextFactory() {
        return evaluationContextFactory;
    }

    /// Sets the [EvaluationContextFactory] which creates Spring [EvaluationContext] instances used for evaluating
    /// expressions in comments and text.
    ///
    /// @param evaluationContextFactory the factory to use.
    ///
    /// @return the configuration object for chaining.
    @Override
    public DocxStamperConfiguration setEvaluationContextFactory(EvaluationContextFactory evaluationContextFactory) {
        this.evaluationContextFactory = evaluationContextFactory;
        return this;
    }

    /// Retrieves the mapping of expression function classes to their corresponding function instances.
    ///
    /// @return a map where the keys are classes representing the function types, and the values are the function
    ///         instances.
    @Override
    public Map<Class<?>, Object> getExpressionFunctions() {
        return expressionFunctions;
    }

    /// Retrieves the map of comment processors associated with specific classes.
    ///
    /// @return a map where the key is the class associated with a specific type of placeholder, and the value is a
    ///         function that creates a [CommentProcessor] for that placeholder.
    @Override
    public Map<Class<?>, CommentProcessorFactory> getCommentProcessors() {
        return commentProcessors;
    }

    /// Retrieves the list of preprocessors.
    ///
    /// @return a list of [PreProcessor] objects.
    @Override
    public List<PreProcessor> getPreprocessors() {
        return preprocessors;
    }

    /// Retrieves the list of object resolvers.
    ///
    /// @return a list of [ObjectResolver] instances.
    @Override
    public List<ObjectResolver> getResolvers() {
        return resolvers;
    }

    /// Sets resolvers for resolving objects in the [DocxStamperConfiguration].
    ///
    /// This method is the evolution of the method [#addResolver(ObjectResolver)], and the order in which the resolvers
    /// are ordered is determinant; the first resolvers in the list will be tried first.
    ///
    ///  If a fallback resolver is desired, it should be placed last in the list.
    ///
    /// @param resolvers The list of [ObjectResolver] to be set.
    ///
    /// @return the configuration object for chaining.
    @Override
    public DocxStamperConfiguration setResolvers(List<ObjectResolver> resolvers) {
        this.resolvers.clear();
        this.resolvers.addAll(resolvers);
        return this;
    }

    /// Adds a resolver to the list of resolvers in the [DocxStamperConfiguration] object.
    ///
    ///  Resolvers are used to resolve objects during the stamping process.
    ///
    /// @param resolver The resolver to be added.
    ///
    /// @return The modified [DocxStamperConfiguration] object, with the resolver added to the beginning of the resolver
    ///         list.
    @Override
    public DocxStamperConfiguration addResolver(ObjectResolver resolver) {
        resolvers.addFirst(resolver);
        return this;
    }

    /// Retrieves the exception resolver.
    ///
    /// @return the current instance of [ExceptionResolver].
    @Override
    public ExceptionResolver getExceptionResolver() {
        return exceptionResolver;
    }

    /// Configures the exception resolver for the [DocxStamperConfiguration].
    ///
    /// @param exceptionResolver the [ExceptionResolver] to handle exceptions during processing
    ///
    /// @return the current instance of [DocxStamperConfiguration]
    @Override
    public DocxStamperConfiguration setExceptionResolver(ExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
        return this;
    }

    /// Retrieves a list of custom functions.
    ///
    /// @return a List containing [CustomFunction] objects.
    @Override
    public List<CustomFunction> customFunctions() {
        return functions;
    }

    /// Adds a custom function to the system, allowing integration of user-defined functionality.
    ///
    /// @param name The name of the custom function being added. This is used as the identifier for the function
    ///         and must be unique across all defined functions.
    /// @param implementation A [Supplier] functional interface that provides the implementation of the custom
    ///         function. When the function is called, the supplier's get method will be executed to return the result
    ///         of the function.
    @Override
    public void addCustomFunction(String name, Supplier<?> implementation) {
        this.addCustomFunction(new CustomFunction(name, List.of(), args -> implementation.get()));
    }

    /// Adds a custom function to the list of functions.
    ///
    /// @param function the [CustomFunction] object to be added
    public void addCustomFunction(CustomFunction function) {
        this.functions.add(function);
    }

    /// Adds a custom function to the context with the specified name and type.
    ///
    /// @param name the name of the custom function
    /// @param class0 the class type of the custom function
    /// @param <T> the type of the input parameter
    ///
    /// @return an instance of [NeedsFunctionImpl] configured with the custom function
    @Override
    public <T> NeedsFunctionImpl<T> addCustomFunction(String name, Class<T> class0) {
        return new FunctionBuilder<>(this, name, class0);
    }

    /// Adds a custom function with the specified name and input types.
    ///
    /// @param name the name of the custom function to be added
    /// @param class0 the class type of the first input parameter of the custom function.
    /// @param class1 the class type of the second input parameter of the custom function.
    /// @param <T> the type of the first input parameter
    /// @param <U> the type of the second input parameter
    ///
    /// @return an instance of [NeedsBiFunctionImpl] for further configuration or usage of the custom function.
    @Override
    public <T, U> NeedsBiFunctionImpl<T, U> addCustomFunction(String name, Class<T> class0, Class<U> class1) {
        return new BiFunctionBuilder<>(this, name, class0, class1);
    }

    /// Adds a custom function to the current context by defining its name, and the classes associated with its argument
    /// types.
    ///
    /// @param name the name to assign to the custom function
    /// @param class0 the class of the first argument type
    /// @param class1 the class of the second argument type
    /// @param class2 the class of the third argument type
    /// @param <T> the type of the first argument
    /// @param <U> the type of the second argument
    /// @param <V> the type of the third argument
    ///
    /// @return an instance of [NeedsTriFunctionImpl] indicating the custom function implementation and usage context.
    @Override
    public <T, U, V> NeedsTriFunctionImpl<T, U, V> addCustomFunction(
            String name,
            Class<T> class0,
            Class<U> class1,
            Class<V> class2
    ) {
        return new TriFunctionBuilder<>(this, name, class0, class1, class2);
    }

    /// Retrieves the list of postprocessors.
    ///
    /// @return a List of [PostProcessor] objects.
    @Override
    public List<PostProcessor> getPostprocessors() {
        return postprocessors;
    }

    /// Adds a given postprocessor to the list of postprocessors.
    ///
    /// @param postprocessor the [PostProcessor] instance to be added
    @Override
    public void addPostprocessor(PostProcessor postprocessor) {
        postprocessors.add(postprocessor);
    }

    @Override
    public ExpressionParser getExpressionParser() {
        return expressionParser;
    }

    /// Sets the expression parser used for expression evaluation.
    ///
    /// Note that the provided parser will be used for all expressions in the document, including expressions in
    /// comments. If you use SpEL, construct a `SpelExpressionParser` (optionally with a `SpelParserConfiguration`) and
    /// pass it here.
    ///
    /// @param expressionParser the parser to use.
    ///
    /// @return the configuration object for chaining.
    @Override
    public DocxStamperConfiguration setExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
        return this;
    }

    /// Resets all processors in the configuration.
    public void resetCommentProcessors() {
        this.commentProcessors.clear();
    }

    /// Resets all resolvers in the configuration.
    public void resetResolvers() {
        this.resolvers.clear();
    }
}
