package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.core.DocxStamperConfiguration;
import pro.verron.officestamper.preset.CommentProcessorFactory.*;
import pro.verron.officestamper.preset.processors.displayif.DisplayIfProcessor;
import pro.verron.officestamper.preset.processors.repeat.RepeatDocPartProcessor;
import pro.verron.officestamper.preset.processors.repeat.RepeatParagraphProcessor;
import pro.verron.officestamper.preset.processors.repeat.RepeatProcessor;
import pro.verron.officestamper.preset.processors.repeatrow.RepeatRowProcessor;
import pro.verron.officestamper.preset.processors.replacewith.ReplaceWithProcessor;
import pro.verron.officestamper.preset.processors.table.TableResolver;

import java.time.temporal.TemporalAccessor;
import java.util.List;

import static java.time.format.DateTimeFormatter.*;
import static java.time.format.FormatStyle.valueOf;
import static java.util.Locale.forLanguageTag;


/// Utility class providing factory methods for various pre-configured instances of [OfficeStamperConfiguration].
///
/// These configurations range from minimal to fully-featured, catering to different use cases for processing Office
/// documents.
public class OfficeStamperConfigurations {

    private OfficeStamperConfigurations() {
        throw new OfficeStamperException("Utility class should not be instantiated");
    }

    /// Creates a full [OfficeStamperConfiguration] with standard configurations, supplemented with additional pre- and
    /// post-processors for enhanced document handling.
    ///
    /// This configuration includes preprocessors to:
    /// - Remove language proof markings.
    /// - Remove language information.
    /// - Merge similar text runs.
    ///
    /// It also includes postprocessors to:
    /// - Remove orphaned footnotes.
    /// - Remove orphaned endnotes.
    ///
    /// @return a fully configured [OfficeStamperConfiguration] instance with the additional processors applied.
    public static OfficeStamperConfiguration full() {
        var configuration = standard();

        configuration.addPreprocessor(Preprocessors.removeLanguageProof());
        configuration.addPreprocessor(Preprocessors.removeLanguageInfo());
        configuration.addPreprocessor(Preprocessors.mergeSimilarRuns());

        configuration.addPostprocessor(Postprocessors.removeOrphanedFootnotes());
        configuration.addPostprocessor(Postprocessors.removeOrphanedEndnotes());

        return configuration;
    }

    /// Creates a standard [OfficeStamperConfiguration] instance with predefined settings.
    ///
    /// The configuration is extended with custom comment processing, resolvers, and additional preprocessors.
    ///
    /// It sets up a fallback resolver with the default value of a newline character ("`\n`") to handle placeholder
    /// resolution.
    ///
    /// @return a standard [OfficeStamperConfiguration] instance with pre-configured resolvers and processors
    public static OfficeStamperConfiguration standard() {
        var fallback = Resolvers.fallback("\n");
        return standard(fallback);
    }

    /// Creates a standard [OfficeStamperConfiguration] instance with a set of predefined comment processors, resolvers,
    /// and preprocessors.
    ///
    /// The configuration is extended with custom functions for date and time formatting, and permits the provision of a
    /// custom fallback resolver.
    ///
    /// @param fallback an [ObjectResolver] to serve as the additional fallback resolver for this
    ///         configuration.
    ///
    /// @return a configured [OfficeStamperConfiguration] object implementing standard processing and formatting
    ///         behaviors
    public static OfficeStamperConfiguration standard(ObjectResolver fallback) {
        var configuration = minimal();

        configuration.addCommentProcessor(IRepeatRowProcessor.class, RepeatRowProcessor::new);
        configuration.addCommentProcessor(IParagraphRepeatProcessor.class, RepeatParagraphProcessor::new);
        configuration.addCommentProcessor(IRepeatDocPartProcessor.class, RepeatDocPartProcessor::new);
        configuration.addCommentProcessor(IRepeatProcessor.class, RepeatProcessor::new);
        configuration.addCommentProcessor(ITableResolver.class, TableResolver::new);
        configuration.addCommentProcessor(IDisplayIfProcessor.class, DisplayIfProcessor::new);
        configuration.addCommentProcessor(IReplaceWithProcessor.class, ReplaceWithProcessor::new);

        configuration.setResolvers(List.of(Resolvers.image(),
                Resolvers.legacyDate(),
                Resolvers.isoDate(),
                Resolvers.isoTime(),
                Resolvers.isoDateTime(),
                Resolvers.nullToEmpty(),
                fallback));

        configuration.addPreprocessor(Preprocessors.removeMalformedComments());

        var fdate = "fdate";
        var ftime = "ftime";
        var fdatetime = "fdatetime";
        var fpattern = "fpattern";
        var flocaldate = "flocaldate";
        var flocaltime = "flocaltime";
        var flocaldatetime = "flocaldatetime";
        var finstant = "finstant";
        var fordinaldate = "fordinaldate";
        var f1123datetime = "f1123datetime";
        var fbasicdate = "fbasicdate";
        var fweekdate = "fweekdate";
        var foffsetdatetime = "foffsetdatetime";
        var fzoneddatetime = "fzoneddatetime";
        var foffsetdate = "foffsetdate";
        var foffsettime = "foffsettime";
        configuration.addCustomFunction(fdate, TemporalAccessor.class)
                     .withImplementation(ISO_DATE::format)
                     .addCustomFunction(ftime, TemporalAccessor.class)
                     .withImplementation(ISO_TIME::format)
                     .addCustomFunction(fdatetime, TemporalAccessor.class)
                     .withImplementation(ISO_DATE_TIME::format)
                     .addCustomFunction(finstant, TemporalAccessor.class)
                     .withImplementation(ISO_INSTANT::format)
                     .addCustomFunction(fordinaldate, TemporalAccessor.class)
                     .withImplementation(ISO_ORDINAL_DATE::format)
                     .addCustomFunction(f1123datetime, TemporalAccessor.class)
                     .withImplementation(RFC_1123_DATE_TIME::format)
                     .addCustomFunction(fbasicdate, TemporalAccessor.class)
                     .withImplementation(BASIC_ISO_DATE::format)
                     .addCustomFunction(fweekdate, TemporalAccessor.class)
                     .withImplementation(ISO_WEEK_DATE::format)
                     .addCustomFunction(flocaldatetime, TemporalAccessor.class)
                     .withImplementation(ISO_LOCAL_DATE_TIME::format)
                     .addCustomFunction(flocaldatetime, TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::flocaldatetime)
                     .addCustomFunction(flocaldatetime, TemporalAccessor.class, String.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::flocaldatetime)
                     .addCustomFunction(foffsetdatetime, TemporalAccessor.class)
                     .withImplementation(ISO_OFFSET_DATE_TIME::format)
                     .addCustomFunction(fzoneddatetime, TemporalAccessor.class)
                     .withImplementation(ISO_ZONED_DATE_TIME::format)
                     .addCustomFunction(foffsetdate, TemporalAccessor.class)
                     .withImplementation(ISO_OFFSET_DATE::format)
                     .addCustomFunction(foffsettime, TemporalAccessor.class)
                     .withImplementation(ISO_OFFSET_TIME::format)
                     .addCustomFunction(flocaldate, TemporalAccessor.class)
                     .withImplementation(ISO_LOCAL_DATE::format)
                     .addCustomFunction(flocaldate, TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::flocaldate)
                     .addCustomFunction(flocaltime, TemporalAccessor.class)
                     .withImplementation(ISO_LOCAL_TIME::format)
                     .addCustomFunction(flocaltime, TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::flocaltime)
                     .addCustomFunction(fpattern, TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::fpattern)
                     .addCustomFunction(fpattern, TemporalAccessor.class, String.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::fpattern);

        return configuration;
    }

    /// Creates a minimal [OfficeStamperConfiguration] instance with essential settings to provide basic placeholder
    /// processing and fallback resolvers.
    ///
    /// This configuration includes:
    /// - A fallback resolver with a default value of a newline character ("`\n`").
    /// - A placeholder preprocessor that prepares placeholders matching a specific pattern.
    ///
    /// @return a minimally configured [OfficeStamperConfiguration] instance
    public static OfficeStamperConfiguration minimal() {
        var configuration = raw();
        configuration.addResolver(Resolvers.fallback("\n"));
        configuration.addPreprocessor(Preprocessors.preparePlaceholders("(\\$\\{([^{]+?)})", "placeholder"));
        configuration.addPreprocessor(Preprocessors.preparePlaceholders("(\\#\\{([^{]+?)})", "inlineProcessor"));
        configuration.addPreprocessor(Preprocessors.prepareCommentProcessor());
        configuration.addPostprocessor(Postprocessors.removeTags("officestamper"));
        return configuration;
    }

    private static Object flocaldatetime(TemporalAccessor date, String style) {
        return ofLocalizedDateTime(valueOf(style)).format(date);
    }

    private static Object flocaldatetime(TemporalAccessor date, String dateStyle, String timeStyle) {
        return ofLocalizedDateTime(valueOf(dateStyle), valueOf(timeStyle)).format(date);
    }

    private static Object flocaldate(TemporalAccessor date, String style) {
        return ofLocalizedDate(valueOf(style)).format(date);
    }

    private static Object flocaltime(TemporalAccessor date, String style) {
        return ofLocalizedTime(valueOf(style)).format(date);
    }

    private static Object fpattern(TemporalAccessor date, String pattern) {
        return ofPattern(pattern).format(date);
    }

    private static Object fpattern(TemporalAccessor date, String pattern, String locale) {
        return ofPattern(pattern, forLanguageTag(locale)).format(date);
    }

    /// Creates a [OfficeStamperConfiguration] instance without any configuration or resolvers, processors,
    /// preprocessors or postprocessors applied.
    ///
    /// @return a basic [OfficeStamperConfiguration] instance with no extra configurations
    public static OfficeStamperConfiguration raw() {
        var defaultFactory = EvaluationContextFactories.defaultFactory();
        var defaultExceptionResolver = ExceptionResolvers.throwing();
        return new DocxStamperConfiguration(defaultFactory, defaultExceptionResolver);
    }
}
