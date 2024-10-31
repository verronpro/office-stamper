package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import java.time.temporal.TemporalAccessor;

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
        configuration.addPreprocessor(Preprocessors.mergeSimilarRuns());
        return configuration;
    }

    /**
     * Creates a new standard OfficeStamperConfiguration.
     *
     * @return the standard OfficeStamperConfiguration
     */
    public static OfficeStamperConfiguration standard() {
        var configuration = new DocxStamperConfiguration();
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
                     .withImplementation((date, pattern, locale) -> ofPattern(pattern, forLanguageTag(locale)).format(
                             date));
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
