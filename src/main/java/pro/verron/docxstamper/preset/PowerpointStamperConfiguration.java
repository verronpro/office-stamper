package pro.verron.docxstamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.docxstamper.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

final class PowerpointStamperConfiguration
        implements OfficeStamperConfiguration {
    @Override
    public Optional<String> nullReplacementValue() {
        return Optional.empty();
    }

    @Override
    public boolean isFailOnUnresolvedExpression() {
        return false;
    }

    @Override
    public OfficeStamperConfiguration setFailOnUnresolvedExpression(
            boolean failOnUnresolvedExpression
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration nullValuesDefault(String nullValuesDefault) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration replaceNullValues(boolean replaceNullValues) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration unresolvedExpressionsDefaultValue(
            String unresolvedExpressionsDefaultValue
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration replaceUnresolvedExpressions(
            boolean replaceUnresolvedExpressions
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration leaveEmptyOnExpressionError(
            boolean leaveEmpty
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <T> OfficeStamperConfiguration addTypeResolver(
            Class<T> resolvedType,
            ITypeResolver<T> resolver
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass,
            Object implementation
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamper<WordprocessingMLPackage> build() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void addPreprocessor(PreProcessor preprocessor) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isReplaceUnresolvedExpressions() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isLeaveEmptyOnExpressionError() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getUnresolvedExpressionsDefaultValue() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getLineBreakPlaceholder() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration setLineBreakPlaceholder(String lineBreakPlaceholder) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public EvaluationContextConfigurer getEvaluationContextConfigurer() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public SpelParserConfiguration getSpelParserConfiguration() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<Class<?>, Object> getExpressionFunctions() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<Class<?>, ITypeResolver<?>> getTypeResolvers() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ITypeResolver<Object> getDefaultTypeResolver() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration setDefaultTypeResolver(
            ITypeResolver<? super Object> defaultResolver
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isReplaceNullValues() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getNullValuesDefault() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public List<PreProcessor> getPreprocessors() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public List<ObjectResolver> getResolvers() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration setResolvers(List<ObjectResolver> resolvers) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public OfficeStamperConfiguration addResolver(ObjectResolver resolver) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
