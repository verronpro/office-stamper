package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface OfficeStamperConfiguration {
    @Deprecated(since = "1.6.7")
    Optional<String> nullReplacementValue();

    boolean isFailOnUnresolvedExpression();

    OfficeStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression);

    @Deprecated(since = "1.6.7", forRemoval = true)
    OfficeStamperConfiguration nullValuesDefault(String nullValuesDefault);

    @Deprecated(since = "1.6.7", forRemoval = true)
    OfficeStamperConfiguration replaceNullValues(boolean replaceNullValues);

    OfficeStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue);

    OfficeStamperConfiguration replaceUnresolvedExpressions(
            boolean replaceUnresolvedExpressions
    );

    OfficeStamperConfiguration leaveEmptyOnExpressionError(
            boolean leaveEmpty
    );

    @Deprecated(since = "1.6.7", forRemoval = true)
    <T> OfficeStamperConfiguration addTypeResolver(
            Class<T> resolvedType, ITypeResolver<T> resolver
    );

    OfficeStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass, Object implementation
    );

    OfficeStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    );

    @Deprecated(forRemoval = true, since = "1.6.4")
    OfficeStamper<WordprocessingMLPackage> build();

    void addPreprocessor(PreProcessor preprocessor);

    boolean isReplaceUnresolvedExpressions();

    boolean isLeaveEmptyOnExpressionError();

    String getUnresolvedExpressionsDefaultValue();

    String getLineBreakPlaceholder();

    OfficeStamperConfiguration setLineBreakPlaceholder(
            @NonNull String lineBreakPlaceholder
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
