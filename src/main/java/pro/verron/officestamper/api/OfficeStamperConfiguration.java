package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents the configuration for the OfficeStamper class.
 */
public interface OfficeStamperConfiguration {
    /**
     * Retrieves the null replacement value.
     *
     * @return an Optional containing the null replacement value if it exists; otherwise, an empty Optional.
     *
     * @deprecated since version 1.6.7
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    Optional<String> nullReplacementValue();

    /**
     * Checks if the failOnUnresolvedExpression flag is set to true or false.
     *
     * @return true if failOnUnresolvedExpression is set to true, false otherwise.
     */
    boolean isFailOnUnresolvedExpression();

    /**
     * Sets the failOnUnresolvedExpression flag to determine whether unresolved expressions should
     * cause an exception to be thrown.
     *
     * @param failOnUnresolvedExpression flag indicating whether to fail on unresolved expressions
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression);

    /**
     * Retrieves a new OfficeStamperConfiguration object with the provided null replacement value.
     *
     * @param nullValuesDefault the null replacement value.
     *
     * @return OfficeStamperConfiguration object with the provided null replacement value.
     *
     * @deprecated since version 1.6.7
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    OfficeStamperConfiguration nullValuesDefault(String nullValuesDefault);

    /**
     * Replaces null values in the OfficeStamperConfiguration object.
     *
     * @param replaceNullValues flag indicating whether to replace null values
     *
     * @return the updated OfficeStamperConfiguration object
     *
     * @deprecated since version 1.6.7
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    OfficeStamperConfiguration replaceNullValues(boolean replaceNullValues);

    /**
     * Sets the default value for unresolved expressions in the OfficeStamperConfiguration object.
     *
     * @param unresolvedExpressionsDefaultValue the default value for unresolved expressions
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue);

    /**
     * Replaces unresolved expressions in the OfficeStamperConfiguration object.
     *
     * @param replaceUnresolvedExpressions flag indicating whether to replace unresolved expressions
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration replaceUnresolvedExpressions(
            boolean replaceUnresolvedExpressions
    );

    /**
     * Configures whether to leave empty on expression error.
     *
     * @param leaveEmpty boolean value indicating whether to leave empty on expression error
     *
     * @return the updated OfficeStamperConfiguration object
     */
    OfficeStamperConfiguration leaveEmptyOnExpressionError(
            boolean leaveEmpty
    );

    /**
     * Adds a type resolver to the OfficeStamperConfiguration.
     * A type resolver is responsible for mapping an object of a certain Java class to an object of the DOCX4J api that
     * can be put into the .docx document. Type resolvers are used to replace expressions within the .docx template.
     *
     * @param resolvedType the Java class that the type resolver is responsible for.
     * @param resolver     the implementation of {@link ITypeResolver} that resolves objects of the given type.
     * @param <T>          type expected to be resolved by the resolver
     *
     * @return the updated OfficeStamperConfiguration object.
     *
     * @deprecated as of version 1.6.7, replaced by {@link ObjectResolver}.
     * The new resolver is more versatile, requires less reflection mechanism,
     * and simplifies the internal workings of the docx-stamper project.
     */
    @Deprecated(since = "1.6.7", forRemoval = true) <T> OfficeStamperConfiguration addTypeResolver(
            Class<T> resolvedType, ITypeResolver<T> resolver
    );

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
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    );

    /**
     * Retrieves the OfficeStamper configured with the current settings.
     *
     * @return the OfficeStamper configured with the current settings.
     *
     * @since 1.6.4
     * @deprecated This method is marked for removal and should not be used. Consider using a different method instead.
     */
    @Deprecated(forRemoval = true, since = "1.6.4")
    OfficeStamper<WordprocessingMLPackage> build();

    /**
     * Adds a pre-processor to the OfficeStamperConfiguration. A pre-processor is responsible for
     * processing the document before the actual processing takes place.
     *
     * @param preprocessor the pre-processor to add
     */
    void addPreprocessor(PreProcessor preprocessor);

    /**
     * Determines whether unresolved expressions in the OfficeStamper configuration should be replaced.
     *
     * @return true if unresolved expressions should be replaced, false otherwise.
     */
    boolean isReplaceUnresolvedExpressions();

    /**
     * Determines whether to leave empty on expression error.
     *
     * @return true if expression errors are left empty, false otherwise
     */
    boolean isLeaveEmptyOnExpressionError();

    /**
     * Retrieves the default value for unresolved expressions.
     *
     * @return the default value for unresolved expressions
     */
    String getUnresolvedExpressionsDefaultValue();

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
    OfficeStamperConfiguration setLineBreakPlaceholder(
            String lineBreakPlaceholder
    );

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
     * Retrieves a map of type resolvers.
     *
     * @return A map containing type resolvers. The keys of the map are classes and the values are instances
     * of ITypeResolver.
     *
     * @deprecated This method has been deprecated since version 1.6.7 and will be removed in a future release.
     * Use of this method is discouraged and should be replaced with an alternative implementation.
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    Map<Class<?>, ITypeResolver<?>> getTypeResolvers();

    /**
     * Retrieves the default type resolver.
     *
     * @return The default type resolver.
     *
     * @deprecated This method has been deprecated since version 1.6.7 and will be removed in a future release.
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    ITypeResolver<Object> getDefaultTypeResolver();

    /**
     * Sets the default type resolver for the OfficeStamperConfiguration.
     * This method is deprecated and will be removed in version 1.6.7.
     *
     * @param defaultResolver the default type resolver to be set
     *
     * @return the OfficeStamperConfiguration with the updated default type resolver
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    OfficeStamperConfiguration setDefaultTypeResolver(
            ITypeResolver<? super Object> defaultResolver
    );

    /**
     * Returns a map of comment processors associated with their respective classes.
     *
     * @return The map of comment processors. The keys are the classes, and the values are the corresponding comment
     * processors.
     */
    Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors();

    /**
     * Determines whether null values should be replaced.
     *
     * @return true if null values should be replaced, false otherwise.
     *
     * @deprecated Since version 1.6.7. This method will be removed in a future release.
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    boolean isReplaceNullValues();

    /**
     * Retrieves the default value for null values.
     *
     * @return The default value for null values.
     *
     * @deprecated This method has been deprecated since version 1.6.7 and will be removed in a future release.
     */
    @Deprecated(since = "1.6.7", forRemoval = true)
    String getNullValuesDefault();

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
    OfficeStamperConfiguration setResolvers(
            List<ObjectResolver> resolvers
    );

    /**
     * Adds an ObjectResolver to the OfficeStamperConfiguration.
     *
     * @param resolver The ObjectResolver to add to the configuration.
     *
     * @return The updated OfficeStamperConfiguration.
     */
    OfficeStamperConfiguration addResolver(
            ObjectResolver resolver
    );
}
