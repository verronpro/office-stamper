package org.wickedsource.docxstamper;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.lang.NonNull;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.el.DefaultEvaluationContextConfigurer;
import org.wickedsource.docxstamper.processor.displayif.IDisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.IParagraphRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.IRepeatDocPartProcessor;
import org.wickedsource.docxstamper.processor.repeat.IRepeatProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.IReplaceWithProcessor;
import org.wickedsource.docxstamper.processor.table.ITableResolver;
import org.wickedsource.docxstamper.replace.typeresolver.TypeResolver;
import pro.verron.docxstamper.api.*;
import pro.verron.docxstamper.preset.CommentProcessorFactory;
import pro.verron.docxstamper.preset.Resolvers;
import pro.verron.docxstamper.preset.resolver.Null2DefaultResolver;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * The {@link DocxStamperConfiguration} class represents the configuration for
 * the {@link DocxStamper} class.
 * It provides methods to customize the behavior of the stamper.
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.3
 */
public class DocxStamperConfiguration
        implements pro.verron.docxstamper.api.DocxStamperConfiguration {

    private final Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> commentProcessors = new HashMap<>();
    private final List<ObjectResolver> resolvers = new ArrayList<>();
    private final Map<Class<?>, Object> expressionFunctions = new HashMap<>();
    private final List<PreProcessor> preprocessors = new ArrayList<>();
    private String lineBreakPlaceholder = "\n";
    private EvaluationContextConfigurer evaluationContextConfigurer = new DefaultEvaluationContextConfigurer();
    private boolean failOnUnresolvedExpression = true;
    private boolean leaveEmptyOnExpressionError = false;
    private boolean replaceUnresolvedExpressions = false;
    private String unresolvedExpressionsDefaultValue = null;
    @Deprecated(since = "1.6.7")
    private boolean replaceNullValues = false;
    @Deprecated(since = "1.6.7")
    private String nullValuesDefault = null;
    private SpelParserConfiguration spelParserConfiguration = new SpelParserConfiguration();

    /**
     * Creates a new configuration with default values.
     */
    public DocxStamperConfiguration() {
        CommentProcessorFactory pf = new CommentProcessorFactory(this);
        commentProcessors.put(IRepeatProcessor.class, pf::repeat);
        commentProcessors.put(IParagraphRepeatProcessor.class,
                              pf::repeatParagraph);
        commentProcessors.put(IRepeatDocPartProcessor.class, pf::repeatDocPart);
        commentProcessors.put(ITableResolver.class, pf::tableResolver);
        commentProcessors.put(IDisplayIfProcessor.class, pf::displayIf);
        commentProcessors.put(IReplaceWithProcessor.class, pf::replaceWith);

        resolvers.addAll(List.of(Resolvers.image(),
                                 Resolvers.legacyDate(),
                                 Resolvers.isoDate(),
                                 Resolvers.isoTime(),
                                 Resolvers.isoDateTime(),
                                 Resolvers.nullToEmpty(),
                                 Resolvers.fallback()));
    }

    /**
     * Retrieves the default replacement value for null values.
     *
     * @return the {@link Optional} containing the default replacement value,
     * or an empty {@link Optional} if no default replacement value is found
     * @deprecated This method's been deprecated since version 1.6.7.
     * You shouldn't have to use it, it was a crutch use for inner working of
     * docx-stamper
     */
    @Override
    @Deprecated(since = "1.6.7")
    public Optional<String> nullReplacementValue() {
        return resolvers.stream()
                .filter(Null2DefaultResolver.class::isInstance)
                .map(Null2DefaultResolver.class::cast)
                .map(Null2DefaultResolver::defaultValue)
                .findFirst();
    }

    /**
     * <p>isFailOnUnresolvedExpression.</p>
     *
     * @return a boolean
     */
    @Override
    public boolean isFailOnUnresolvedExpression() {
        return failOnUnresolvedExpression;
    }

    /**
     * If set to true, stamper will throw an {@link DocxStamperException}
     * if a variable expression or processor expression within the document or within the comments is encountered that cannot be resolved. Is set to true by default.
     *
     * @param failOnUnresolvedExpression a boolean
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override
    public DocxStamperConfiguration setFailOnUnresolvedExpression(boolean failOnUnresolvedExpression) {
        this.failOnUnresolvedExpression = failOnUnresolvedExpression;
        return this;
    }

    /**
     * Sets the default value for null values in the document.
     *
     * @param nullValuesDefault The default value for null values.
     * @return The updated DocxStamperConfiguration object.
     * @deprecated This method has been deprecated since version 1.6.7.
     * It is recommended to use
     * {@link DocxStamperConfiguration#addResolver(ObjectResolver)} and
     * {@link Resolvers#nullToDefault(String)} instead.
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public DocxStamperConfiguration nullValuesDefault(String nullValuesDefault) {
        this.nullValuesDefault = nullValuesDefault;
        this.resolvers.add(0, this.replaceNullValues
                ? Resolvers.nullToDefault(this.nullValuesDefault)
                : Resolvers.nullToPlaceholder());
        return this;
    }

    /**
     * Replaces null values with either empty string or a placeholder value,
     * based on the given flag.
     *
     * @param replaceNullValues Flag indicating whether to replace null values or not.
     * @return The updated DocxStamperConfiguration object.
     * @deprecated This method has been deprecated since version 1.6.7.
     * It is recommended to use
     * {@link DocxStamperConfiguration#addResolver(ObjectResolver)} and
     * {@link Resolvers#nullToDefault(String)} instead.
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public DocxStamperConfiguration replaceNullValues(boolean replaceNullValues) {
        this.replaceNullValues = replaceNullValues;
        this.resolvers.add(this.replaceNullValues
                                   ? Resolvers.nullToEmpty()
                                   : Resolvers.nullToPlaceholder());
        return this;
    }

    /**
     * Indicates the default value to use for expressions that doesn't resolve.
     *
     * @param unresolvedExpressionsDefaultValue value to use instead for expression that doesn't resolve
     * @return a {@link DocxStamperConfiguration} object
     * @see DocxStamperConfiguration#replaceUnresolvedExpressions
     */
    @Override
    public DocxStamperConfiguration unresolvedExpressionsDefaultValue(String unresolvedExpressionsDefaultValue) {
        this.unresolvedExpressionsDefaultValue = unresolvedExpressionsDefaultValue;
        return this;
    }

    /**
     * Indicates if a default value should replace expressions that don't resolve.
     *
     * @param replaceUnresolvedExpressions true to replace null value expression with resolved value (which is null), false to leave the expression as is
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override
    public DocxStamperConfiguration replaceUnresolvedExpressions(boolean replaceUnresolvedExpressions) {
        this.replaceUnresolvedExpressions = replaceUnresolvedExpressions;
        return this;
    }

    /**
     * If an error is caught while evaluating an expression, the expression will be replaced with an empty string instead
     * of leaving the original expression in the document.
     *
     * @param leaveEmpty true to replace expressions with empty string when an error is caught while evaluating
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override
    public DocxStamperConfiguration leaveEmptyOnExpressionError(boolean leaveEmpty) {
        this.leaveEmptyOnExpressionError = leaveEmpty;
        return this;
    }

    /**
     * <p>
     * Registers the given {@link ITypeResolver} for the given class. The
     * registered {@link ITypeResolver#resolve(WordprocessingMLPackage, Object)} method will only
     * be called with objects of the specified class.
     * </p>
     * <p>
     * Note that each type can only be resolved by ONE ITypeResolver implementation. Multiple calls to addTypeResolver()
     * with the same resolvedType parameter will override earlier calls.
     * </p>
     *
     * @param resolvedType the class whose objects are to be passed to the given ITypeResolver.
     * @param resolver     the resolver to resolve objects of the given type.
     * @param <T>          the type resolved by the ITypeResolver.
     * @return a {@link DocxStamperConfiguration} object
     * @deprecated This method has been deprecated since version 1.6.7, and
     * it is scheduled for removal.
     * It's recommended to use the {@link DocxStamperConfiguration#addResolver(ObjectResolver)}
     * method for adding resolvers.
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public <T> DocxStamperConfiguration addTypeResolver(
            Class<T> resolvedType, ITypeResolver<T> resolver
    ) {
        this.resolvers.add(0, new TypeResolver<>(resolvedType, resolver));
        return this;
    }

    /**
     * Exposes all methods of a given interface to the expression language.
     *
     * @param interfaceClass the interface whose methods should be exposed in the expression language.
     * @param implementation the implementation that should be called to evaluate invocations of the interface methods
     *                       within the expression language. Must implement the interface above.
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override
    public DocxStamperConfiguration exposeInterfaceToExpressionLanguage(
            Class<?> interfaceClass, Object implementation
    ) {
        this.expressionFunctions.put(interfaceClass, implementation);
        return this;
    }

    /**
     * Registers the specified ICommentProcessor as an implementation of the
     * specified interface.
     *
     * @param interfaceClass          the Interface which is implemented by the commentProcessor.
     * @param commentProcessorFactory the commentProcessor factory generating the specified interface.
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override
    public DocxStamperConfiguration addCommentProcessor(
            Class<?> interfaceClass,
            Function<ParagraphPlaceholderReplacer, CommentProcessor> commentProcessorFactory
    ) {
        this.commentProcessors.put(interfaceClass, commentProcessorFactory);
        return this;
    }

    /**
     * Creates a {@link DocxStamper} instance configured with this configuration.
     *
     * @param <T> a T class
     * @return a {@link DocxStamper} object
     * @deprecated use new {@link DocxStamper#DocxStamper(DocxStamperConfiguration)} instead
     */
    @Override
    @Deprecated(forRemoval = true, since = "1.6.4")
    public OpcStamper<WordprocessingMLPackage> build() {
        return new DocxStamper<>(this);
    }

    /**
     * Adds a preprocessor to the configuration.
     *
     * @param preprocessor the preprocessor to add.
     */
    @Override
    public void addPreprocessor(PreProcessor preprocessor) {
        preprocessors.add(preprocessor);
    }

    /**
     * <p>isReplaceUnresolvedExpressions.</p>
     *
     * @return a boolean
     */
    @Override
    public boolean isReplaceUnresolvedExpressions() {
        return replaceUnresolvedExpressions;
    }

    /**
     * <p>isLeaveEmptyOnExpressionError.</p>
     *
     * @return a boolean
     */
    @Override
    public boolean isLeaveEmptyOnExpressionError() {
        return leaveEmptyOnExpressionError;
    }

    /**
     * <p>Getter for the field <code>unresolvedExpressionsDefaultValue</code>.</p>
     *
     * @return a {@link String} object
     */
    @Override
    public String getUnresolvedExpressionsDefaultValue() {
        return unresolvedExpressionsDefaultValue;
    }

    /**
     * <p>Getter for the field <code>lineBreakPlaceholder</code>.</p>
     *
     * @return a {@link String} object
     */
    @Override
    public String getLineBreakPlaceholder() {
        return lineBreakPlaceholder;
    }

    /**
     * The String provided as lineBreakPlaceholder will be replaced with a line break
     * when stamping a document. If no lineBreakPlaceholder is provided, no replacement
     * will take place.
     *
     * @param lineBreakPlaceholder the String that should be replaced with line breaks during stamping.
     * @return the configuration object for chaining.
     */
    @Override
    public DocxStamperConfiguration setLineBreakPlaceholder(@NonNull String lineBreakPlaceholder) {
        this.lineBreakPlaceholder = lineBreakPlaceholder;
        return this;
    }

    /**
     * <p>Getter for the field <code>evaluationContextConfigurer</code>.</p>
     *
     * @return a {@link EvaluationContextConfigurer} object
     */
    @Override
    public EvaluationContextConfigurer getEvaluationContextConfigurer() {
        return evaluationContextConfigurer;
    }

    /**
     * Provides an {@link EvaluationContextConfigurer} which may change the configuration of a Spring
     * {@link EvaluationContext} which is used for evaluating expressions
     * in comments and text.
     *
     * @param evaluationContextConfigurer the configurer to use.
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override
    public DocxStamperConfiguration setEvaluationContextConfigurer(
            EvaluationContextConfigurer evaluationContextConfigurer
    ) {
        this.evaluationContextConfigurer = evaluationContextConfigurer;
        return this;
    }

    /**
     * <p>Getter for the field <code>spelParserConfiguration</code>.</p>
     *
     * @return a {@link SpelParserConfiguration} object
     */
    @Override
    public SpelParserConfiguration getSpelParserConfiguration() {
        return spelParserConfiguration;
    }

    /**
     * Sets the {@link SpelParserConfiguration} to use for expression parsing.
     * <p>
     * Note that this configuration will be used for all expressions in the document, including expressions in comments!
     * </p>
     *
     * @param spelParserConfiguration the configuration to use.
     * @return a {@link DocxStamperConfiguration} object
     */
    @Override
    public DocxStamperConfiguration setSpelParserConfiguration(
            SpelParserConfiguration spelParserConfiguration
    ) {
        this.spelParserConfiguration = spelParserConfiguration;
        return this;
    }

    /**
     * <p>Getter for the field <code>expressionFunctions</code>.</p>
     *
     * @return a {@link Map} object
     */
    @Override
    public Map<Class<?>, Object> getExpressionFunctions() {
        return expressionFunctions;
    }

    /**
     * Retrieves the map of type resolvers.
     *
     * @return the map of type resolvers
     * @deprecated This method's been deprecated since version 1.6.7
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public Map<Class<?>, ITypeResolver<?>> getTypeResolvers() {
        return resolvers.stream()
                .filter(resolver -> resolver instanceof TypeResolver)
                .map(TypeResolver.class::cast)
                .collect(toMap(TypeResolver::type, TypeResolver::resolver));
    }

    /**
     * Returns the default type resolver.
     * <p>
     * This method returns the default type resolver from a list of resolvers. It filters the resolvers to only include instances of
     * {@link ITypeResolver}, and returns the first one found. If no type resolver is found, null is returned.
     *
     * @return the default type resolver, or null if not found
     * @deprecated This method's been deprecated since version 1.6.7 and is scheduled for removal.
     * You should not have to use it, it was a clutch for previous version of
     * docx-stamper.
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public ITypeResolver<Object> getDefaultTypeResolver() {
        return (ITypeResolver<Object>) resolvers.stream()
                .filter(ITypeResolver.class::isInstance)
                .map(ITypeResolver.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Sets the default type resolver for the DocxStamperConfiguration.
     *
     * @param defaultResolver The default type resolver to set.
     * @return The updated DocxStamperConfiguration instance.
     * @deprecated This method is deprecated and scheduled for removal in version 1.6.7.
     * Please set your "default" resolver through
     * {@link DocxStamperConfiguration#addResolver(ObjectResolver)} by
     * putting your default first in the list, or by using
     * {@link DocxStamperConfiguration#setResolvers(List)}} and putting your
     * fallback resolvers in the last place.
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public DocxStamperConfiguration setDefaultTypeResolver(
            ITypeResolver<? super Object> defaultResolver
    ) {
        var resolver = new TypeResolver<>(Object.class, defaultResolver, true);
        this.resolvers.add(resolver);
        return this;
    }

    /**
     * <p>Getter for the field <code>commentProcessors</code>.</p>
     *
     * @return a {@link Map} object
     */
    @Override
    public Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> getCommentProcessors() {
        return commentProcessors;
    }

    /**
     * Gets the flag indicating whether null values should be replaced.
     *
     * @return {@code true} if null values should be replaced, {@code false} otherwise.
     * @deprecated This method's been deprecated since version 1.6.7 and will be removed in a future release.
     * You shouldn't have to use it, it was a clutch for
     * docx-stamper workings.
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public boolean isReplaceNullValues() {
        return replaceNullValues;
    }

    /**
     * Retrieves the default value used for representing null values.
     *
     * @return the default value for null values
     * @deprecated This method has been deprecated since version 1.6.7 and is scheduled for removal.
     * You shouldn't have to use it, it was a clutch for docx-stamper workings.
     */
    @Override
    @Deprecated(since = "1.6.7", forRemoval = true)
    public String getNullValuesDefault() {
        return nullValuesDefault;
    }

    /**
     * <p>Getter for the field <code>preprocessors</code>.</p>
     *
     * @return a {@link List} object
     */
    @Override
    public List<PreProcessor> getPreprocessors() {
        return preprocessors;
    }

    /**
     * Retrieves the list of resolvers.
     *
     * @return The list of object resolvers.
     */
    @Override
    public List<ObjectResolver> getResolvers() {
        return resolvers;
    }

    /**
     * Sets the resolvers for resolving objects in the DocxStamperConfiguration.
     * <p>
     * This method is the evolution of the method {@code addTypeResolver},
     * and the order in which the resolvers are ordered is determinant - the first resolvers
     * in the list will be tried first. If a fallback resolver is desired, it should be placed last in the list.
     *
     * @param resolvers The list of ObjectResolvers to be set.
     * @return The updated DocxStamperConfiguration instance.
     */
    @Override
    public DocxStamperConfiguration setResolvers(
            List<ObjectResolver> resolvers
    ) {
        this.resolvers.clear();
        this.resolvers.addAll(resolvers);
        return this;
    }

    /**
     * Adds a resolver to the list of resolvers in the `DocxStamperConfiguration` object.
     * Resolvers are used to resolve objects during the stamping process.
     *
     * @param resolver The resolver to be added. This resolver should implement the `ObjectResolver` interface.
     * @return The modified `DocxStamperConfiguration` object, with the resolver added to the beginning of the resolver list.
     */
    @Override
    public DocxStamperConfiguration addResolver(ObjectResolver resolver) {
        resolvers.add(0, resolver);
        return this;
    }
}
