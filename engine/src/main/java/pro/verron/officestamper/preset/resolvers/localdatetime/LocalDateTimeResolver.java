package pro.verron.officestamper.preset.resolvers.localdatetime;

import pro.verron.officestamper.api.StringResolver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Resolves {@link LocalDateTime} values to a formatted string.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.4
 */
public final class LocalDateTimeResolver
        extends StringResolver<LocalDateTime> {
    private final DateTimeFormatter formatter;

    /**
     * Creates a new resolver that uses {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} to format
     * {@link LocalDateTime}
     * values.
     */
    public LocalDateTimeResolver() {
        this(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Creates a new resolver that uses the given formatter to format {@link LocalDateTime} values.
     *
     * @param formatter the formatter to use.
     */
    public LocalDateTimeResolver(DateTimeFormatter formatter) {
        super(LocalDateTime.class);
        this.formatter = formatter;
    }

    /** {@inheritDoc} */
    @Override
    protected String resolve(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }
}
