package pro.verron.officestamper.preset.resolvers.localdatetime;

import pro.verron.officestamper.api.StringResolver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/// Resolves [LocalDateTime] values to a formatted string.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public final class LocalDateTimeResolver
        extends StringResolver<LocalDateTime> {
    private final DateTimeFormatter formatter;

    /// Creates a new resolver that uses [#ISO_LOCAL_DATE_TIME] to format
    /// [LocalDateTime]
    /// values.
    public LocalDateTimeResolver() {
        this(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /// Creates a new resolver that uses the given formatter to format [LocalDateTime] values.
    ///
    /// @param formatter the formatter to use.
    public LocalDateTimeResolver(DateTimeFormatter formatter) {
        super(LocalDateTime.class);
        this.formatter = formatter;
    }

    @Override
    protected String resolve(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }
}
