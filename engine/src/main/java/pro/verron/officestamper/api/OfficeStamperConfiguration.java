package pro.verron.officestamper.api;

import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.CustomFunction.NeedsBiFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsFunctionImpl;
import pro.verron.officestamper.api.CustomFunction.NeedsTriFunctionImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


/// Interface representing the configuration for the Office Stamper functionality.
public interface OfficeStamperConfiguration {

    /// Exposes an interface to the expression language.
    ///
    /// @param interfaceClass the interface class to be exposed
    /// @param implementation the implementation object of the interface
    ///
    /// @return the updated OfficeStamperConfiguration object
    OfficeStamperConfiguration exposeInterfaceToExpressionLanguage(Class<?> interfaceClass, Object implementation);

    /// Adds a comment processor to the OfficeStamperConfiguration. A comment processor is responsible for
    /// processing comments in the document and performing specific operations based on the comment content.
    ///
    /// @param interfaceClass          the interface class associated with the comment processor
    /// @param commentProcessorFactory a function that creates a CommentProcessor object based on the
    ///
    ///                                ParagraphPlaceholderReplacer implementation
    ///
    /// @return the updated OfficeStamperConfiguration object
    OfficeStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    );

    /// Adds a pre-processor to the OfficeStamperConfiguration. A pre-processor is responsible for
    /// processing the document before the actual processing takes place.
    ///
    /// @param preprocessor the pre-processor to add
    OfficeStamperConfiguration addPreprocessor(PreProcessor preprocessor);

    /// Retrieves the EvaluationContextConfigurer for configuring the Spring Expression Language (SPEL)
    ///  EvaluationContext
    /// used by the docxstamper.
    ///
    /// @return the EvaluationContextConfigurer for configuring the SPEL EvaluationContext.
    EvaluationContextConfigurer getEvaluationContextConfigurer();

    /// Sets the EvaluationContextConfigurer for configuring the Spring Expression Language (SPEL) EvaluationContext.
    ///
    /// @param evaluationContextConfigurer the EvaluationContextConfigurer for configuring the SPEL EvaluationContext.
    ///                                    Must implement the evaluateEvaluationContext() method.
    ///
    /// @return the updated OfficeStamperConfiguration object.
    OfficeStamperConfiguration setEvaluationContextConfigurer(EvaluationContextConfigurer evaluationContextConfigurer);

    /// Retrieves the SpelParserConfiguration used by the OfficeStamperConfiguration.
    ///
    /// @return the SpelParserConfiguration object used by the OfficeStamperConfiguration.
    SpelParserConfiguration getSpelParserConfiguration();

    /// Sets the SpelParserConfiguration used by the OfficeStamperConfiguration.
    ///
    /// @param spelParserConfiguration the SpelParserConfiguration to be set
    ///
    /// @return the updated OfficeStamperConfiguration object
    OfficeStamperConfiguration setSpelParserConfiguration(SpelParserConfiguration spelParserConfiguration);

    /// Retrieves the map of expression functions associated with their corresponding classes.
    ///
    /// @return a map containing the expression functions as values and their corresponding classes as keys.
    Map<Class<?>, Object> getExpressionFunctions();

    /// Returns a map of comment processors associated with their respective classes.
    ///
    /// @return The map of comment processors. The keys are the classes, and the values are the corresponding comment
    /// processors.
    Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors();

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

    ExceptionResolver getExceptionResolver();

    OfficeStamperConfiguration setExceptionResolver(ExceptionResolver exceptionResolver);

    List<CustomFunction> customFunctions();

    void addCustomFunction(String name, Supplier<?> implementation);

    <T> NeedsFunctionImpl<T> addCustomFunction(String name, Class<T> class0);

    <T, U> NeedsBiFunctionImpl<T, U> addCustomFunction(String name, Class<T> class0, Class<U> class1);

    <T, U, V> NeedsTriFunctionImpl<T, U, V> addCustomFunction(
            String name,
            Class<T> class0,
            Class<U> class1,
            Class<V> class2
    );

    List<PostProcessor> getPostprocessors();

    OfficeStamperConfiguration addPostprocessor(PostProcessor postProcessor);
}
