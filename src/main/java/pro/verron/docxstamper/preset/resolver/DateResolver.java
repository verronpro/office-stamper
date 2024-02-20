package pro.verron.docxstamper.preset.resolver;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * This ITypeResolver creates a formatted date String for expressions that return a Date object.
 *
 * @author Joseph Verron
 * @version 1.6.7
 */
public class DateResolver
        extends StringResolver<Date> {

    private final DateTimeFormatter formatter;

    /**
     * Creates a new DateResolver that uses the format "dd.MM.yyyy".
     */
    public DateResolver() {
        this(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    /**
     * Creates a new DateResolver.
     *
     * @param formatter the format to use for date formatting. See java.text.SimpleDateFormat.
     */
    public DateResolver(DateTimeFormatter formatter) {
        super(Date.class);
        this.formatter = formatter;
    }

    /**
     * Resolves a formatted date string for the given Date object.
     *
     * @param date the Date object to be resolved.
     * @return the formatted date string.
     */
    @Override
    protected String resolveStringForObject(Date date) {
        var zone = ZoneId.systemDefault();
        var localDate = date.toInstant()
                .atZone(zone)
                .toLocalDate();
        return formatter.format(localDate);
    }
}
