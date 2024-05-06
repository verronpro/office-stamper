package pro.verron.docxstamper.api;

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
     * @return an Optional containing the null replacement value, if it exists; otherwise, an empty Optional.
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

    OfficeStamperConfiguration setLineBreakPlaceholder(
            String lineBreakPlaceholder
    );

    EvaluationContextConfigurer getEvaluationContextConfigurer();

    OfficeStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    );

    SpelParserConfiguration getSpelParserConfiguration();

    OfficeStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    );

    Map<Class<?>, Object> getExpressionFunctions();

    @Deprecated(since = "1.6.7", forRemoval = true)
    Map<Class<?>, ITypeResolver<?>> getTypeResolvers();

    @Deprecated(since = "1.6.7", forRemoval = true)
    ITypeResolver<Object> getDefaultTypeResolver();

    @Deprecated(since = "1.6.7", forRemoval = true)
    OfficeStamperConfiguration setDefaultTypeResolver(
            ITypeResolver<? super Object> defaultResolver
    );

    Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors();

    @Deprecated(since = "1.6.7", forRemoval = true)
    boolean isReplaceNullValues();

    @Deprecated(since = "1.6.7", forRemoval = true)
    String getNullValuesDefault();

    List<PreProcessor> getPreprocessors();

    List<ObjectResolver> getResolvers();

    OfficeStamperConfiguration setResolvers(
            List<ObjectResolver> resolvers
    );

    OfficeStamperConfiguration addResolver(
            ObjectResolver resolver
    );
}
