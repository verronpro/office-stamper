package pro.verron.officestamper.preset.resolvers.localtime;

import pro.verron.officestamper.api.StringResolver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/// Resolves [LocalTime] values to the format specified by the [DateTimeFormatter] passed to the constructor.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public final class LocalTimeResolver
        extends StringResolver<LocalTime> {
    private final DateTimeFormatter formatter;

    /// Uses [#ISO_LOCAL_TIME] for formatting.
    public LocalTimeResolver() {
        this(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    /// Constructor for LocalTimeResolver.
    ///
    /// @param formatter a date time pattern as specified by [#ofPattern(String)]
    public LocalTimeResolver(DateTimeFormatter formatter) {
        super(LocalTime.class);
        this.formatter = formatter;
    }

    @Override
    protected String resolve(LocalTime localTime) {
        return localTime.format(formatter);
    }
}
