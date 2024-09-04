package pro.verron.officestamper.preset;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

class StamperDateFormatter
        implements IStamperDateFormatter {

    @Override public String fdate(TemporalAccessor date) {
        return DateTimeFormatter.ISO_DATE.format(date);
    }

    @Override public String fdatetime(TemporalAccessor date) {
        return DateTimeFormatter.ISO_DATE_TIME.format(date);
    }

    @Override public String finstant(TemporalAccessor date) {
        return DateTimeFormatter.ISO_INSTANT.format(date);
    }

    @Override public String flocaldate(TemporalAccessor date, String style) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.valueOf(style))
                                .format(date);
    }

    @Override public String fpattern(TemporalAccessor date, String pattern, String locale) {
        return DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag(locale))
                                .format(date);
    }

    @Override public String flocaltime(TemporalAccessor date, String style) {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.valueOf(style))
                                .format(date);
    }

    @Override public String flocaldatetime(TemporalAccessor date, String style) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.valueOf(style))
                                .format(date);
    }

    @Override public String flocaldate(TemporalAccessor date) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    @Override public String fordinaldate(TemporalAccessor date) {
        return DateTimeFormatter.ISO_ORDINAL_DATE.format(date);
    }

    @Override public String f1123datetime(TemporalAccessor date) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(date);
    }

    @Override public String flocaldatetime(TemporalAccessor date, String dateStyle, String timeStyle) {
        return DateTimeFormatter.ofLocalizedDateTime(
                                        FormatStyle.valueOf(dateStyle),
                                        FormatStyle.valueOf(timeStyle))
                                .format(date);
    }

    @Override public String fbasicdate(TemporalAccessor date) {
        return DateTimeFormatter.BASIC_ISO_DATE.format(date);
    }

    @Override public String fweekdate(TemporalAccessor date) {
        return DateTimeFormatter.ISO_WEEK_DATE.format(date);
    }

    @Override public String flocaldatetime(TemporalAccessor date) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date);
    }

    @Override public String foffsetdatetime(TemporalAccessor date) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date);
    }

    @Override public String fzoneddatetime(TemporalAccessor date) {
        return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(date);
    }

    @Override public String foffsetdate(TemporalAccessor date) {
        return DateTimeFormatter.ISO_OFFSET_DATE.format(date);
    }

    @Override public String flocaltime(TemporalAccessor date) {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(date);
    }

    @Override public String foffsettime(TemporalAccessor date) {
        return DateTimeFormatter.ISO_OFFSET_TIME.format(date);
    }

    @Override public String ftime(TemporalAccessor date) {
        return DateTimeFormatter.ISO_TIME.format(date);
    }

    @Override public String fpattern(TemporalAccessor date, String pattern) {
        return DateTimeFormatter.ofPattern(pattern)
                                .format(date);
    }
}
