package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface OpcStamperConfiguration {
    @Deprecated(since = "1.6.7")
    Optional<String> nullReplacementValue();

    boolean isFailOnUnresolvedExpression();

    OpcStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression);

    @Deprecated(since = "1.6.7", forRemoval = true)
    OpcStamperConfiguration nullValuesDefault(String nullValuesDefault);

    @Deprecated(since = "1.6.7", forRemoval = true)
    OpcStamperConfiguration replaceNullValues(boolean replaceNullValues);

    OpcStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue);

    OpcStamperConfiguration replaceUnresolvedExpressions(
            boolean replaceUnresolvedExpressions
    );

    OpcStamperConfiguration leaveEmptyOnExpressionError(
            boolean leaveEmpty
    );

    @Deprecated(since = "1.6.7", forRemoval = true)
    <T> OpcStamperConfiguration addTypeResolver(
            Class<T> resolvedType, ITypeResolver<T> resolver
    );

    OpcStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass, Object implementation
    );

    OpcStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    );

    @Deprecated(forRemoval = true, since = "1.6.4")
    OpcStamper<WordprocessingMLPackage> build();

    void addPreprocessor(PreProcessor preprocessor);

    boolean isReplaceUnresolvedExpressions();

    boolean isLeaveEmptyOnExpressionError();

    String getUnresolvedExpressionsDefaultValue();

    String getLineBreakPlaceholder();

    OpcStamperConfiguration setLineBreakPlaceholder(
            @NonNull String lineBreakPlaceholder
    );

    EvaluationContextConfigurer getEvaluationContextConfigurer();

    OpcStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    );

    SpelParserConfiguration getSpelParserConfiguration();

    OpcStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    );

    Map<Class<?>, Object> getExpressionFunctions();

    @Deprecated(since = "1.6.7", forRemoval = true)
    Map<Class<?>, ITypeResolver<?>> getTypeResolvers();

    @Deprecated(since = "1.6.7", forRemoval = true)
    ITypeResolver<Object> getDefaultTypeResolver();

    @Deprecated(since = "1.6.7", forRemoval = true)
    OpcStamperConfiguration setDefaultTypeResolver(
            ITypeResolver<? super Object> defaultResolver
    );

    Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors();

    @Deprecated(since = "1.6.7", forRemoval = true)
    boolean isReplaceNullValues();

    @Deprecated(since = "1.6.7", forRemoval = true)
    String getNullValuesDefault();

    List<PreProcessor> getPreprocessors();

    List<ObjectResolver> getResolvers();

    OpcStamperConfiguration setResolvers(
            List<ObjectResolver> resolvers
    );

    OpcStamperConfiguration addResolver(
            ObjectResolver resolver
    );
}
