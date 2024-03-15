package pro.verron.docxstamper.api;

import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.lang.NonNull;
import org.wickedsource.docxstamper.DocxStamper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface DocxStamperConfiguration {
    @Deprecated(since = "1.6.7")
    Optional<String> nullReplacementValue();

    boolean isFailOnUnresolvedExpression();

    DocxStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression);

    @Deprecated(since = "1.6.7", forRemoval = true)
    DocxStamperConfiguration nullValuesDefault(String nullValuesDefault);

    @Deprecated(since = "1.6.7", forRemoval = true)
    DocxStamperConfiguration replaceNullValues(boolean replaceNullValues);

    DocxStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue);

    DocxStamperConfiguration replaceUnresolvedExpressions(
            boolean replaceUnresolvedExpressions
    );

    DocxStamperConfiguration leaveEmptyOnExpressionError(
            boolean leaveEmpty
    );

    @Deprecated(since = "1.6.7", forRemoval = true)
    <T> DocxStamperConfiguration addTypeResolver(
            Class<T> resolvedType, ITypeResolver<T> resolver
    );

    DocxStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass, Object implementation
    );

    DocxStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    );

    @Deprecated(forRemoval = true, since = "1.6.4")
    <T> DocxStamper<T> build();

    void addPreprocessor(PreProcessor preprocessor);

    boolean isReplaceUnresolvedExpressions();

    boolean isLeaveEmptyOnExpressionError();

    String getUnresolvedExpressionsDefaultValue();

    String getLineBreakPlaceholder();

    DocxStamperConfiguration setLineBreakPlaceholder(
            @NonNull String lineBreakPlaceholder
    );

    EvaluationContextConfigurer getEvaluationContextConfigurer();

    DocxStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    );

    SpelParserConfiguration getSpelParserConfiguration();

    DocxStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    );

    Map<Class<?>, Object> getExpressionFunctions();

    @Deprecated(since = "1.6.7", forRemoval = true)
    Map<Class<?>, ITypeResolver<?>> getTypeResolvers();

    @Deprecated(since = "1.6.7", forRemoval = true)
    ITypeResolver<Object> getDefaultTypeResolver();

    @Deprecated(since = "1.6.7", forRemoval = true)
    DocxStamperConfiguration setDefaultTypeResolver(
            ITypeResolver<? super Object> defaultResolver
    );

    Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors();

    @Deprecated(since = "1.6.7", forRemoval = true)
    boolean isReplaceNullValues();

    @Deprecated(since = "1.6.7", forRemoval = true)
    String getNullValuesDefault();

    List<PreProcessor> getPreprocessors();

    List<ObjectResolver> getResolvers();

    DocxStamperConfiguration setResolvers(
            List<ObjectResolver> resolvers
    );

    DocxStamperConfiguration addResolver(
            ObjectResolver resolver
    );
}
