package pro.verron.officestamper.api;

import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.CustomFunction.NeedsBiFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsTriFunctionImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Interface representing the configuration for the Office Stamper functionality.
 */
public interface OfficeStamperConfiguration {
    /**
     * Checks if the failOnUnresolvedExpression flag is set to true or false.
     *
     * @return true if failOnUnresolvedExpression is set to true, false otherwise.
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true) boolean isFailOnUnresolvedExpression();

    /**
     * Sets the failOnUnresolvedExpression flag to determine whether unresolved expressions should
     * cause an exception to be thrown.
     *
     * @param failOnUnresolvedExpression flag indicating whether to fail on unresolved expressions
     *
     * @return the updated OfficeStamperConfiguration object
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true)
    OfficeStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression);

    /**
     * Determines whether to leave empty on expression error.
     *
     * @return true if expression errors are left empty, false otherwise
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true) boolean isLeaveEmptyOnExpressionError();

    /**
     * Determines whether unresolved expressions in the OfficeStamper configuration should be replaced.
     *
     * @return true if unresolved expressions should be replaced, false otherwise.
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true) boolean isReplaceUnresolvedExpressions();

    /**
     * Retrieves the default value for unresolved expressions.
     *
     * @return the default value for unresolved expressions
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true) String getUnresolvedExpressionsDefaultValue();

    /**
     * Sets the default value for unresolved expressions in the OfficeStamperConfiguration object.
     *
     * @param unresolvedExpressionsDefaultValue the default value for unresolved expressions
     *
     * @return the updated OfficeStamperConfiguration object
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true)
    OfficeStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue);

    /**
     * Replaces unresolved expressions in the OfficeStamperConfiguration object.
     *
     * @param replaceUnresolvedExpressions flag indicating whether to replace unresolved expressions
     *
     * @return the updated OfficeStamperConfiguration object
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true) OfficeStamperConfiguration replaceUnresolvedExpressions(
            boolean replaceUnresolvedExpressions
    );

    /**
     * Configures whether to leave empty on expression error.
     *
     * @param leaveEmpty boolean value indicating whether to leave empty on expression error
     *
     * @return the updated OfficeStamperConfiguration object
     *
     * @deprecated This method is deprecated because it offers limited functionality by just checking a flag.
     * It is replaced by {@link #setExceptionResolver(ExceptionResolver)} , which provides
     * complete customization over the behavior during resolution failures. The new method
     * allows you to define how unresolved expressions are handled in a more flexible and
     * comprehensive manner.
     */
    @Deprecated(since = "2.5", forRemoval = true)
    OfficeStamperConfiguration leaveEmptyOnExpressionError(boolean leaveEmpty);

    /**
     * Exposes an interface to the expression language.
     *
     * @param interfaceClass the interface class to be exposed
     * @param implementation the implementation object of the interface
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass, Object implementation
    );

    /**
     * Adds a comment processor to the OfficeStamperConfiguration. A comment processor is responsible for
     * processing comments in the document and performing specific operations based on the comment content.
     *
     * @param interfaceClass          the interface class associated with the comment processor
     * @param commentProcessorFactory a function that creates a CommentProcessor object based on the
     *                                ParagraphPlaceholderReplacer implementation
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass, Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    );

    /**
     * Adds a pre-processor to the OfficeStamperConfiguration. A pre-processor is responsible for
     * processing the document before the actual processing takes place.
     *
     * @param preprocessor the pre-processor to add
     */
    void addPreprocessor(PreProcessor preprocessor);

    /**
     * Retrieves the line break placeholder used in the OfficeStamper configuration.
     *
     * @return the line break placeholder as a String.
     */
    String getLineBreakPlaceholder();

    /**
     * Sets the line break placeholder used in the OfficeStamper configuration.
     *
     * @param lineBreakPlaceholder the line break placeholder as a String
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration setLineBreakPlaceholder(String lineBreakPlaceholder);

    /**
     * Retrieves the EvaluationContextConfigurer for configuring the Spring Expression Language (SPEL) EvaluationContext
     * used by the docxstamper.
     *
     * @return the EvaluationContextConfigurer for configuring the SPEL EvaluationContext.
     */
    EvaluationContextConfigurer getEvaluationContextConfigurer();

    /**
     * Sets the EvaluationContextConfigurer for configuring the Spring Expression Language (SPEL) EvaluationContext.
     *
     * @param evaluationContextConfigurer the EvaluationContextConfigurer for configuring the SPEL EvaluationContext.
     *                                    Must implement the evaluateEvaluationContext() method.
     *
     * @return the updated OfficeStamperConfiguration object.
     */
    OfficeStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    );

    /**
     * Retrieves the SpelParserConfiguration used by the OfficeStamperConfiguration.
     *
     * @return the SpelParserConfiguration object used by the OfficeStamperConfiguration.
     */
    SpelParserConfiguration getSpelParserConfiguration();

    /**
     * Sets the SpelParserConfiguration used by the OfficeStamperConfiguration.
     *
     * @param spelParserConfiguration the SpelParserConfiguration to be set
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    );

    /**
     * Retrieves the map of expression functions associated with their corresponding classes.
     *
     * @return a map containing the expression functions as values and their corresponding classes as keys.
     */
    Map<Class<?>, Object> getExpressionFunctions();

    /**
     * Returns a map of comment processors associated with their respective classes.
     *
     * @return The map of comment processors. The keys are the classes, and the values are the corresponding comment
     * processors.
     */
    Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors();

    /**
     * Retrieves the list of pre-processors.
     *
     * @return The list of pre-processors.
     */
    List<PreProcessor> getPreprocessors();

    /**
     * Retrieves the list of ObjectResolvers.
     *
     * @return The list of ObjectResolvers.
     */
    List<ObjectResolver> getResolvers();

    /**
     * Sets the list of object resolvers for the OfficeStamper configuration.
     *
     * @param resolvers the list of object resolvers to be set
     *
     * @return the updated OfficeStamperConfiguration instance
     */
    OfficeStamperConfiguration setResolvers(List<ObjectResolver> resolvers);

    /**
     * Adds an ObjectResolver to the OfficeStamperConfiguration.
     *
     * @param resolver The ObjectResolver to add to the configuration.
     *
     * @return The updated OfficeStamperConfiguration.
     */
    OfficeStamperConfiguration addResolver(ObjectResolver resolver);

    /**
     * Retrieves the instance of the ExceptionResolver.
     *
     * @return the ExceptionResolver instance used to handle exceptions
     */
    ExceptionResolver getExceptionResolver();

    /**
     * Sets the exception resolver to be used by the OfficeStamperConfiguration.
     * The exception resolver determines how exceptions will be handled during the
     * processing of office documents.
     *
     * @param exceptionResolver the ExceptionResolver instance to set
     * @return the current instance of OfficeStamperConfiguration for method chaining
     */
    OfficeStamperConfiguration setExceptionResolver(ExceptionResolver exceptionResolver);

    /**
     * Retrieves a list of custom functions.
     *
     * @return a List containing instances of CustomFunction.
     */
    List<CustomFunction> customFunctions();

    /**
     * Adds a custom function to the system with the specified name and implementation.
     *
     * @param name          the unique name of the custom function to be added
     * @param implementation a Supplier that provides the implementation of the custom function
     */
    void addCustomFunction(String name, Supplier<?> implementation);

    /**
     * Adds a custom function with the specified name and associated class type.
     * This method allows users to define custom behavior by associating a function
     * implementation with a given name and type.
     *
     * @param <T>  The type associated with the custom function.
     * @param name The name of the custom function to be added.
     * @param class0 The class type of the custom function.
     * @return An instance of NeedsFunctionImpl parameterized with the type of the custom function.
     */
    <T> NeedsFunctionImpl<T> addCustomFunction(String name, Class<T> class0);

    /**
     * Adds a custom bi-function with the specified name and the provided parameter types.
     *
     * @param name the name of the custom function to be added
     * @param class0 the class type for the first parameter of the bi-function
     * @param class1 the class type for the second parameter of the bi-function
     * @return an instance of NeedsBiFunctionImpl parameterized with the provided types
     */
    <T, U> NeedsBiFunctionImpl<T, U> addCustomFunction(String name, Class<T> class0, Class<U> class1);

    /**
     * Adds a custom function with the specified parameters.
     *
     * @param name the name of the custom function
     * @param class0 the class type of the first parameter
     * @param class1 the class type of the second parameter
     * @param class2 the class type of the third parameter
     * @param <T> the type of the first parameter
     * @param <U> the type of the second parameter
     * @param <V> the type of the third parameter
     * @return an instance of NeedsTriFunctionImpl for the provided parameter types
     */
    <T, U, V> NeedsTriFunctionImpl<T, U, V> addCustomFunction(
            String name, Class<T> class0, Class<U> class1, Class<V> class2
    );

    /**
     * Retrieves the list of post-processors associated with this instance.
     *
     * @return a list of PostProcessor objects.
     */
    List<PostProcessor> getPostprocessors();

    /**
     * Adds a postprocessor to modify or enhance data or operations during
     * the processing lifecycle.
     *
     * @param postProcessor the PostProcessor instance to be added
     */
    void addPostprocessor(PostProcessor postProcessor);

}
