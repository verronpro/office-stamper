package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.core.DocxStamperConfiguration;
import pro.verron.officestamper.preset.CommentProcessorFactory.*;
import pro.verron.officestamper.preset.processors.displayif.DisplayIfProcessor;
import pro.verron.officestamper.preset.processors.repeat.RepeatProcessor;
import pro.verron.officestamper.preset.processors.repeatdocpart.RepeatDocPartProcessor;
import pro.verron.officestamper.preset.processors.repeatparagraph.ParagraphRepeatProcessor;
import pro.verron.officestamper.preset.processors.replacewith.ReplaceWithProcessor;
import pro.verron.officestamper.preset.processors.table.TableResolver;

import java.time.temporal.TemporalAccessor;
import java.util.List;

import static java.time.format.DateTimeFormatter.*;
import static java.time.format.FormatStyle.valueOf;
import static java.util.Locale.forLanguageTag;


/// The OfficeStamperConfigurations class provides static methods
/// to create different configurations for the OfficeStamper.
public class OfficeStamperConfigurations {

    private OfficeStamperConfigurations() {
        throw new OfficeStamperException("OfficeStamperConfigurations cannot be instantiated");
    }

    /// Creates a new standard OfficeStamperConfiguration.
    ///
    /// @return the standard OfficeStamperConfiguration
    public static OfficeStamperConfiguration standard() {
        var configuration = new DocxStamperConfiguration();

        configuration.addCommentProcessor(IRepeatProcessor.class, RepeatProcessor::newInstance)
                     .addCommentProcessor(IParagraphRepeatProcessor.class, ParagraphRepeatProcessor::newInstance)
                     .addCommentProcessor(IRepeatDocPartProcessor.class,
                             pr -> RepeatDocPartProcessor.newInstance(pr, configuration))
                     .addCommentProcessor(ITableResolver.class, TableResolver::newInstance)
                     .addCommentProcessor(IDisplayIfProcessor.class, DisplayIfProcessor::newInstance)
                     .addCommentProcessor(IReplaceWithProcessor.class, ReplaceWithProcessor::newInstance);

        configuration.setResolvers(List.of(Resolvers.image(),
                Resolvers.legacyDate(),
                Resolvers.isoDate(),
                Resolvers.isoTime(),
                Resolvers.isoDateTime(),
                Resolvers.nullToEmpty(),
                Resolvers.fallback()));

        configuration.addPreprocessor(Preprocessors.removeLanguageProof())
                     .addPreprocessor(Preprocessors.removeLanguageInfo())
                     .addPreprocessor(Preprocessors.mergeSimilarRuns())
                     .addPreprocessor(Preprocessors.removeMalformedComments());

        configuration.addPostprocessor(Postprocessors.removeOrphanedFootnotes())
                     .addPostprocessor(Postprocessors.removeOrphanedEndnotes());
        configuration.addPostprocessor(Postprocessors.linebreaker("\n"));

        configuration.addCustomFunction("ftime", TemporalAccessor.class)
                     .withImplementation(ISO_TIME::format);
        configuration.addCustomFunction("fdate", TemporalAccessor.class)
                     .withImplementation(ISO_DATE::format);
        configuration.addCustomFunction("fdatetime", TemporalAccessor.class)
                     .withImplementation(ISO_DATE_TIME::format);
        configuration.addCustomFunction("finstant", TemporalAccessor.class)
                     .withImplementation(ISO_INSTANT::format);
        configuration.addCustomFunction("fordinaldate", TemporalAccessor.class)
                     .withImplementation(ISO_ORDINAL_DATE::format);
        configuration.addCustomFunction("f1123datetime", TemporalAccessor.class)
                     .withImplementation(RFC_1123_DATE_TIME::format);
        configuration.addCustomFunction("flocaldate", TemporalAccessor.class)
                     .withImplementation(ISO_LOCAL_DATE::format);
        configuration.addCustomFunction("fbasicdate", TemporalAccessor.class)
                     .withImplementation(BASIC_ISO_DATE::format);
        configuration.addCustomFunction("fweekdate", TemporalAccessor.class)
                     .withImplementation(ISO_WEEK_DATE::format);
        configuration.addCustomFunction("flocaldatetime", TemporalAccessor.class)
                     .withImplementation(ISO_LOCAL_DATE_TIME::format);
        configuration.addCustomFunction("flocaldatetime", TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::localizedDatetimeFormatter);
        configuration.addCustomFunction("flocaldatetime", TemporalAccessor.class, String.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::localizedDatetimeFormatter);
        configuration.addCustomFunction("foffsetdatetime", TemporalAccessor.class)
                     .withImplementation(ISO_OFFSET_DATE_TIME::format);
        configuration.addCustomFunction("fzoneddatetime", TemporalAccessor.class)
                     .withImplementation(ISO_ZONED_DATE_TIME::format);
        configuration.addCustomFunction("foffsetdate", TemporalAccessor.class)
                     .withImplementation(ISO_OFFSET_DATE::format);
        configuration.addCustomFunction("flocaltime", TemporalAccessor.class)
                     .withImplementation(ISO_LOCAL_TIME::format);
        configuration.addCustomFunction("foffsettime", TemporalAccessor.class)
                     .withImplementation(ISO_OFFSET_TIME::format);
        configuration.addCustomFunction("flocaldate", TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::localDateFormatter);
        configuration.addCustomFunction("flocaltime", TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::localTimeFormatter);
        configuration.addCustomFunction("fpattern", TemporalAccessor.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::patternFormatter);
        configuration.addCustomFunction("fpattern", TemporalAccessor.class, String.class, String.class)
                     .withImplementation(OfficeStamperConfigurations::patternFormatter);
        return configuration;
    }


    private static Object localizedDatetimeFormatter(TemporalAccessor date, String style) {
        return ofLocalizedDateTime(valueOf(style)).format(date);
    }

    private static Object localizedDatetimeFormatter(TemporalAccessor date, String dateStyle, String timeStyle) {
        return ofLocalizedDateTime(valueOf(dateStyle), valueOf(timeStyle)).format(date);
    }

    private static Object localDateFormatter(TemporalAccessor date, String style) {
        return ofLocalizedDate(valueOf(style)).format(date);
    }

    private static Object localTimeFormatter(TemporalAccessor date, String style) {
        return ofLocalizedTime(valueOf(style)).format(date);
    }

    private static Object patternFormatter(TemporalAccessor date, String pattern) {
        return ofPattern(pattern).format(date);
    }

    private static Object patternFormatter(TemporalAccessor date, String pattern, String locale) {
        return ofPattern(pattern, forLanguageTag(locale)).format(date);
    }

    /// Creates a new standard OfficeStamperConfiguration.
    ///
    /// @return the standard OfficeStamperConfiguration
    public static OfficeStamperConfiguration raw() {
        var configuration = new DocxStamperConfiguration();
        configuration.resetResolvers();
        configuration.setEvaluationContextConfigurer(EvaluationContextConfigurers.defaultConfigurer());
        configuration.resetCommentProcessors();
        return configuration;
    }
}
