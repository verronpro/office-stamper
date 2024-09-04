package pro.verron.officestamper.preset;

import java.time.temporal.TemporalAccessor;

public interface IStamperDateFormatter {
    String fdate(TemporalAccessor date);

    String fdatetime(TemporalAccessor date);

    String finstant(TemporalAccessor date);

    String flocaldate(TemporalAccessor date, String style);

    String fpattern(TemporalAccessor date, String pattern, String locale);

    String flocaltime(TemporalAccessor date, String style);

    String flocaldatetime(TemporalAccessor date, String style);

    String flocaldate(TemporalAccessor date);

    String fordinaldate(TemporalAccessor date);

    String f1123datetime(TemporalAccessor date);

    String flocaldatetime(TemporalAccessor date, String dateStyle, String timeStyle);

    String fbasicdate(TemporalAccessor date);

    String fweekdate(TemporalAccessor date);

    String flocaldatetime(TemporalAccessor date);

    String foffsetdatetime(TemporalAccessor date);

    String fzoneddatetime(TemporalAccessor date);

    String foffsetdate(TemporalAccessor date);

    String flocaltime(TemporalAccessor date);

    String foffsettime(TemporalAccessor date);

    String ftime(TemporalAccessor date);

    String fpattern(TemporalAccessor date, String pattern);
}
