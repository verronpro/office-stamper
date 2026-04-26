package pro.verron.officestamper.api;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.CustomFunction.NeedsBiFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsTriFunctionImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


/// Interface representing the configuration for the Office Stamper functionality.
public interface OfficeStamperConfiguration {

    /// Exposes an interface to the expression language.
    ///
    /// @param interfaceClass the interface class to be exposed
    /// @param implementation the implementation object of the interface
    ///
    /// @return the updated [OfficeStamperConfiguration] object
    OfficeStamperConfiguration exposeInterfaceToExpressionLanguage(Class<?> interfaceClass, Object implementation);

    /// Adds a comment processor to the [OfficeStamperConfiguration].
    ///
    /// A comment processor is responsible for processing comments in the document and performing specific operations
    /// based on the comment content.
    ///
    /// @param interfaceClass the interface class associated with the comment processor
    /// @param commentProcessorFactory a factory that creates a [CommentProcessor] object
    ///
    /// @return the updated [OfficeStamperConfiguration] object
    OfficeStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            CommentProcessorFactory commentProcessorFactory
    );

    /// Adds a pre-processor to the [OfficeStamperConfiguration].
    ///
    ///  A pre-processor is responsible for processing the document before the actual processing takes place.
    ///
    /// @param preprocessor the pre-processor to add
    void addPreprocessor(PreProcessor preprocessor);

    /// Retrieves the [EvaluationContextFactory] for creating Spring Expression Language (SpEL) [EvaluationContext]
    /// instances used by the office stamper.
    ///
    /// @return the [EvaluationContextFactory] for creating SpEL EvaluationContext instances.
    /// @deprecated since version 3.4, use alternative configuration method [#getSpelSecurityMode()]
    @Deprecated(since = "3.4", forRemoval = true)
    EvaluationContextFactory getEvaluationContextFactory();

    /// Sets the [EvaluationContextFactory] for creating Spring Expression Language (SpEL) EvaluationContext instances.
    ///
    /// @param evaluationContextFactory the [EvaluationContextFactory] for creating SpEL [EvaluationContext]
    ///         instances.
    ///
    /// @return the updated [OfficeStamperConfiguration] object.
    /// @deprecated since version 3.4, use alternative configuration method [#setSpelSecurityMode(SecurityMode)]
    @Deprecated(since = "3.4", forRemoval = true)
    OfficeStamperConfiguration setEvaluationContextFactory(EvaluationContextFactory evaluationContextFactory);

    /// Retrieves the map of expression functions associated with their corresponding classes.
    ///
    /// @return a map containing the expression functions as values and their corresponding classes as keys.
    Map<Class<?>, Object> getExpressionFunctions();

    /// Returns a map of comment processors associated with their respective classes.
    ///
    /// @return The map of comment processors. The keys are the classes, and the values are the corresponding comment
    ///         processor factories.
    Map<Class<?>, CommentProcessorFactory> getCommentProcessors();

    /// Retrieves the list of pre-processors.
    ///
    /// @return The list of pre-processors.
    List<PreProcessor> getPreprocessors();

    /// Retrieves the list of ObjectResolvers.
    ///
    /// @return The list of ObjectResolvers.
    List<ObjectResolver> getResolvers();

    /// Sets the list of object resolvers for the OfficeStamper configuration.
    ///
    /// @param resolvers the list of object resolvers to be set
    ///
    /// @return the updated OfficeStamperConfiguration instance
    OfficeStamperConfiguration setResolvers(List<ObjectResolver> resolvers);

    /// Adds an ObjectResolver to the OfficeStamperConfiguration.
    ///
    /// @param resolver The ObjectResolver to add to the configuration.
    ///
    /// @return The updated OfficeStamperConfiguration.
    OfficeStamperConfiguration addResolver(ObjectResolver resolver);

    /// Retrieves the instance of the ExceptionResolver.
    ///
    /// @return the ExceptionResolver instance used to handle exceptions
    ExceptionResolver getExceptionResolver();

    /// Sets the exception resolver to be used by the OfficeStamperConfiguration. The exception resolver determines how
    /// exceptions will be handled during the processing of office documents.
    ///
    /// @param exceptionResolver the ExceptionResolver instance to set
    ///
    /// @return the current instance of OfficeStamperConfiguration for method chaining
    OfficeStamperConfiguration setExceptionResolver(ExceptionResolver exceptionResolver);

    /// Retrieves a list of custom functions.
    ///
    /// @return a List containing instances of CustomFunction.
    List<CustomFunction> customFunctions();

    /// Adds a custom function to the system with the specified name and implementation.
    ///
    /// @param name the unique name of the custom function to be added
    /// @param implementation a Supplier that provides the implementation of the custom function
    void addCustomFunction(String name, Supplier<?> implementation);

    /// Adds a custom function with the specified name and associated class type. This method allows users to define
    /// custom behavior by associating a function implementation with a given name and type.
    ///
    /// @param <T> The type associated with the custom function.
    /// @param name The name of the custom function to be added.
    /// @param class0 The class type of the custom function.
    ///
    /// @return An instance of NeedsFunctionImpl parameterized with the type of the custom function.
    <T> NeedsFunctionImpl<T> addCustomFunction(String name, Class<T> class0);

    /// Adds a custom bi-function with the specified name and the provided parameter types.
    ///
    /// @param name the name of the custom function to be added
    /// @param class0 the class type for the first parameter of the bi-function
    /// @param class1 the class type for the second parameter of the bi-function
    /// @param <T> the type of the first parameter
    /// @param <U> the type of the second parameter
    ///
    /// @return an instance of NeedsBiFunctionImpl parameterized with the provided types
    <T, U> NeedsBiFunctionImpl<T, U> addCustomFunction(String name, Class<T> class0, Class<U> class1);

    /// Adds a custom function with the specified parameters.
    ///
    /// @param name the name of the custom function
    /// @param class0 the class type of the first parameter
    /// @param class1 the class type of the second parameter
    /// @param class2 the class type of the third parameter
    /// @param <T> the type of the first parameter
    /// @param <U> the type of the second parameter
    /// @param <V> the type of the third parameter
    ///
    /// @return an instance of NeedsTriFunctionImpl for the provided parameter types
    <T, U, V> NeedsTriFunctionImpl<T, U, V> addCustomFunction(
            String name,
            Class<T> class0,
            Class<U> class1,
            Class<V> class2
    );

    /// Retrieves the list of post-processors associated with this instance.
    ///
    /// @return a list of PostProcessor objects.
    List<PostProcessor> getPostprocessors();

    /// Adds a postprocessor to modify or enhance data or operations during the processing lifecycle.
    ///
    /// @param postProcessor the PostProcessor instance to be added
    void addPostprocessor(PostProcessor postProcessor);

    /// Retrieves the parser configuration used by the office stamper.
    ///
    /// @return the [SpelParserConfiguration] instance.
    SpelParserConfiguration getParserConfiguration();

    /// Sets the parser configuration to be used by the office stamper.
    ///
    /// @param parserConfiguration the [SpelParserConfiguration] instance to set.
    ///
    /// @return the updated [OfficeStamperConfiguration] object.
    OfficeStamperConfiguration setParserConfiguration(SpelParserConfiguration parserConfiguration);

    /// Gets the current SpEL security mode.
    ///
    /// Defaults to [SecurityMode.RESTRICTED], which hardens the evaluation context to mitigate
    /// risks such as type access, bean resolution, and constructor invocation.
    ///
    /// @return the current [SecurityMode] used for SpEL evaluation
    SecurityMode getSpelSecurityMode();

    /// Sets the SpEL security mode.
    ///
    /// Use [SecurityMode.RESTRICTED] for untrusted templates (default).
    /// Use [SecurityMode.PERMISSIVE] only for trusted inputs when you need full SpEL capabilities.
    ///
    /// @param mode the desired [SecurityMode]
    /// @return the updated [OfficeStamperConfiguration] object
    OfficeStamperConfiguration setSpelSecurityMode(SecurityMode mode);

    /// Indicates whether SVG safe mode is enabled.
    ///
    /// Defaults to [SecurityMode.RESTRICTED], which performs SVG parsing with hardened XML parser settings to
    /// mitigate XXE/DTD and related risks. When disabled, a more permissive parser is used.
    ///
    /// @return the current [SecurityMode] used for SVG parsing
    SecurityMode getSvgSecurityMode();

    /// Enables or disables SVG safe mode.
    ///
    /// Safe mode is enabled by default to ensure secure SVG parsing. Disable only if you fully trust the SVG inputs
    /// and need permissive behavior.
    ///
    /// @param mode the desired [SecurityMode]
    /// @return the updated [OfficeStamperConfiguration] object
    OfficeStamperConfiguration setSvgSecurityMode(SecurityMode mode);
}
