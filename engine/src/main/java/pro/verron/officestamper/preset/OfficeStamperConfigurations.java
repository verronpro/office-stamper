package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.core.DocxStamperConfiguration;
import pro.verron.officestamper.preset.CommentProcessorFactory.*;
import pro.verron.officestamper.preset.preprocessors.googledocscomments.RealignGoogleDocsCommentAnchors;
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


/**
 * The OfficeStamperConfigurations class provides static methods
 * to create different configurations for the OfficeStamper.
 */
public class OfficeStamperConfigurations {


    private OfficeStamperConfigurations() {
        throw new OfficeStamperException("OfficeStamperConfigurations cannot be instantiated");
    }

    /**
     * Creates a new OfficeStamperConfiguration with the standard configuration and additional preprocessors.
     *
     * @return the OfficeStamperConfiguration
     *
     * @see OfficeStamperConfiguration
     */
    public static OfficeStamperConfiguration standardWithPreprocessing() {
        var configuration = standard();
        configuration.addPreprocessor(Preprocessors.removeLanguageProof());
        configuration.addPreprocessor(Preprocessors.removeLanguageInfo());
        configuration.addPreprocessor(Preprocessors.mergeSimilarRuns());
        configuration.addPostprocessor(Postprocessors.removeOrphanedFootnotes());
        configuration.addPostprocessor(Postprocessors.removeOrphanedEndnotes());
        return configuration;
    }

    /**
     * Creates a new standard OfficeStamperConfiguration.
     *
     * @return the standard OfficeStamperConfiguration
     */
    public static OfficeStamperConfiguration standard() {
        var configuration = new DocxStamperConfiguration();

        configuration.addCommentProcessor(IRepeatProcessor.class, RepeatProcessor::newInstance);
        configuration.addCommentProcessor(IParagraphRepeatProcessor.class, ParagraphRepeatProcessor::newInstance);
        configuration.addCommentProcessor(IRepeatDocPartProcessor.class,
                pr -> RepeatDocPartProcessor.newInstance(pr,
                        (template, context, output) -> new DocxStamper(configuration)
                                .stamp(template, context, output)));
        configuration.addCommentProcessor(ITableResolver.class, TableResolver::newInstance);
        configuration.addCommentProcessor(IDisplayIfProcessor.class, DisplayIfProcessor::newInstance);
        configuration.addCommentProcessor(IReplaceWithProcessor.class, ReplaceWithProcessor::newInstance);

        configuration.setResolvers(List.of(Resolvers.image(),
                Resolvers.legacyDate(),
                Resolvers.isoDate(),
                Resolvers.isoTime(),
                Resolvers.isoDateTime(),
                Resolvers.nullToEmpty(),
                Resolvers.fallback()));

        configuration.addPreprocessor(Preprocessors.realignGoogleDocsCommentAnchors());
        configuration.addPreprocessor(Preprocessors.removeMalformedComments());

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
                     .withImplementation((date, style) -> ofLocalizedDateTime(valueOf(style)).format(date));
        configuration.addCustomFunction("flocaldatetime", TemporalAccessor.class, String.class, String.class)
                     .withImplementation((date, dateStyle, timeStyle) -> ofLocalizedDateTime(valueOf(dateStyle),
                             valueOf(timeStyle)).format(date));
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
                     .withImplementation((date, style) -> ofLocalizedDate(valueOf(style)).format(date));
        configuration.addCustomFunction("flocaltime", TemporalAccessor.class, String.class)
                     .withImplementation((date, style) -> ofLocalizedTime(valueOf(style)).format(date));
        configuration.addCustomFunction("fpattern", TemporalAccessor.class, String.class)
                     .withImplementation((date, pattern) -> ofPattern(pattern).format(date));
        configuration.addCustomFunction("fpattern", TemporalAccessor.class, String.class, String.class)
                     .withImplementation((date, pattern, locale) -> ofPattern(pattern, forLanguageTag(locale))
                             .format(date));
        return configuration;
    }

    /**
     * Creates a new standard OfficeStamperConfiguration.
     *
     * @return the standard OfficeStamperConfiguration
     */
    public static OfficeStamperConfiguration raw() {
        var configuration = new DocxStamperConfiguration();
        configuration.resetResolvers();
        configuration.setEvaluationContextConfigurer(EvaluationContextConfigurers.defaultConfigurer());
        configuration.resetCommentProcessors();
        return configuration;
    }
}
