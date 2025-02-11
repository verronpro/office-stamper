package pro.verron.officestamper.preset.resolvers.localdate;

import pro.verron.officestamper.api.StringResolver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/// Resolves [LocalDate] objects by formatting them with a [DateTimeFormatter].
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public final class LocalDateResolver
        extends StringResolver<LocalDate> {
    private final DateTimeFormatter formatter;

    /// Uses [#ISO_LOCAL_DATE] for formatting.
    public LocalDateResolver() {
        this(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /// Uses the given formatter for formatting.
    ///
    /// @param formatter the formatter to use.
    public LocalDateResolver(DateTimeFormatter formatter) {
        super(LocalDate.class);
        this.formatter = formatter;
    }

    @Override
    protected String resolve(LocalDate localDateTime) {
        return localDateTime.format(formatter);
    }
}
