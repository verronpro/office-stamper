package pro.verron.docxstamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.wickedsource.docxstamper.preprocessor.MergeSameStyleRuns;
import org.wickedsource.docxstamper.preprocessor.RemoveProofErrors;
import pro.verron.docxstamper.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class OfficeStamperConfigurations {
    public static OfficeStamperConfiguration standard() {
        return new org.wickedsource.docxstamper.DocxStamperConfiguration();
    }

    public static OfficeStamperConfiguration standardWithPreprocessing() {
        var configuration = standard();
        configuration.addPreprocessor(new RemoveProofErrors());
        configuration.addPreprocessor(new MergeSameStyleRuns());
        return configuration;
    }

    public static OfficeStamperConfiguration powerpoint() {
        return new OfficeStamperConfiguration() {
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
                return null;
            }

            @Override
            public OfficeStamperConfiguration nullValuesDefault(String nullValuesDefault) {
                return null;
            }

            @Override
            public OfficeStamperConfiguration replaceNullValues(boolean replaceNullValues) {
                return null;
            }

            @Override
            public OfficeStamperConfiguration unresolvedExpressionsDefaultValue(
                    String unresolvedExpressionsDefaultValue
            ) {
                return null;
            }

            @Override
            public OfficeStamperConfiguration replaceUnresolvedExpressions(
                    boolean replaceUnresolvedExpressions
            ) {
                return null;
            }

            @Override
            public OfficeStamperConfiguration leaveEmptyOnExpressionError(
                    boolean leaveEmpty
            ) {
                return null;
            }

            @Override
            public <T> OfficeStamperConfiguration addTypeResolver(
                    Class<T> resolvedType,
                    ITypeResolver<T> resolver
            ) {
                return null;
            }

            @Override
            public OfficeStamperConfiguration exposeInterfaceToExpressionLanguage(
                    Class<?> interfaceClass,
                    Object implementation
            ) {
                return null;
            }

            @Override
            public OfficeStamperConfiguration addCommentProcessor(
                    Class<?> interfaceClass,
                    Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
            ) {
                return null;
            }

            @Override
            public OfficeStamper<WordprocessingMLPackage> build() {
                return null;
            }

            @Override
            public void addPreprocessor(PreProcessor preprocessor) {

            }

            @Override
            public boolean isReplaceUnresolvedExpressions() {
                return false;
            }

            @Override
            public boolean isLeaveEmptyOnExpressionError() {
                return false;
            }

            @Override
            public String getUnresolvedExpressionsDefaultValue() {
                return null;
            }

            @Override
            public String getLineBreakPlaceholder() {
                return null;
            }

            @Override
            public OfficeStamperConfiguration setLineBreakPlaceholder(String lineBreakPlaceholder) {
                return null;
            }

            @Override
            public EvaluationContextConfigurer getEvaluationContextConfigurer() {
                return null;
            }

            @Override
            public OfficeStamperConfiguration setEvaluationContextConfigurer(
                    EvaluationContextConfigurer evaluationContextConfigurer
            ) {
                return null;
            }

            @Override
            public SpelParserConfiguration getSpelParserConfiguration() {
                return null;
            }

            @Override
            public OfficeStamperConfiguration setSpelParserConfiguration(
                    SpelParserConfiguration spelParserConfiguration
            ) {
                return null;
            }

            @Override
            public Map<Class<?>, Object> getExpressionFunctions() {
                return null;
            }

            @Override
            public Map<Class<?>, ITypeResolver<?>> getTypeResolvers() {
                return null;
            }

            @Override
            public ITypeResolver<Object> getDefaultTypeResolver() {
                return null;
            }

            @Override
            public OfficeStamperConfiguration setDefaultTypeResolver(
                    ITypeResolver<? super Object> defaultResolver
            ) {
                return null;
            }

            @Override
            public Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors() {
                return null;
            }

            @Override
            public boolean isReplaceNullValues() {
                return false;
            }

            @Override
            public String getNullValuesDefault() {
                return null;
            }

            @Override
            public List<PreProcessor> getPreprocessors() {
                return null;
            }

            @Override
            public List<ObjectResolver> getResolvers() {
                return null;
            }

            @Override
            public OfficeStamperConfiguration setResolvers(List<ObjectResolver> resolvers) {
                return null;
            }

            @Override
            public OfficeStamperConfiguration addResolver(ObjectResolver resolver) {
                return null;
            }
        };
    }
}
