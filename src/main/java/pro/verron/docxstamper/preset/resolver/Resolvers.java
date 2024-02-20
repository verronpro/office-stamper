package pro.verron.docxstamper.preset.resolver;

import pro.verron.docxstamper.api.ObjectResolver;

import java.time.format.DateTimeFormatter;

/**
 * The Resolvers class provides static methods to create different types of ObjectResolvers.
 *
 * @author Joseph Verron
 * @version 1.6.7
 * @since 1.6.7
 */
public class Resolvers {

    /**
     * Returns an instance of ObjectResolver that acts as a fallback resolver.
     * It uses the ToStringResolver implementation of ObjectResolver.
     *
     * @return An instance of ObjectResolver
     */
    public static ObjectResolver fallback() {
        return new ToStringResolver();
    }

    /**
     * Returns an instance of ObjectResolver that replaces null values with an empty string.
     *
     * @return An instance of ObjectResolver
     */
    public static ObjectResolver nullToEmpty() {
        return nullToDefault("");
    }

    /**
     * Returns an instance of ObjectResolver that resolves null objects
     * by creating a run with a default text value.
     *
     * @param value The default value for null objects.
     * @return An instance of ObjectResolver
     */
    public static ObjectResolver nullToDefault(String value) {
        return new Null2DefaultResolver(value);
    }

    /**
     * Returns an instance of ObjectResolver that resolves null objects
     * by not replacing their placeholder string.
     *
     * @return An instance of ObjectResolver
     */
    public static ObjectResolver nullToPlaceholder() {
        return new Null2PlaceholderResolver();
    }

    /**
     * Returns an instance of {@link LocalTimeResolver}.
     * The LocalTimeResolver class is an implementation of the {@link ObjectResolver} interface
     * that resolves {@link java.time
     */
    public static LocalTimeResolver isoDateTime() {
        return new LocalTimeResolver();
    }

    /**
     * Returns an instance of {@link LocalDateTimeResolver}.
     * The LocalDateTimeResolver class is an implementation of the {@link ObjectResolver} interface
     * that resolves {@link java.time.LocalDateTime} values to a formatted string.
     *
     * @return An instance of LocalDateTimeResolver
     */
    public static LocalDateTimeResolver isoTime() {
        return new LocalDateTimeResolver();
    }

    /**
     * Creates a new instance of LocalDateTimeResolver using the given formatter.
     *
     * @param formatter the DateTimeFormatter to use for formatting LocalDateTime values
     * @return a new instance of LocalDateTimeResolver
     */
    public static LocalDateTimeResolver isoTime(DateTimeFormatter formatter) {
        return new LocalDateTimeResolver(formatter);
    }

    /**
     * Returns an instance of {@link LocalDateResolver}.
     * The LocalDateResolver class is an implementation of the {@link StringResolver} interface
     * that resolves {@link java.time.LocalDate} objects by formatting them with a {@link DateTimeFormatter}.
     *
     * @return An instance of LocalDateResolver
     */
    public static LocalDateResolver isoDate() {
        return new LocalDateResolver();
    }

    /**
     * Returns an instance of LocalDateResolver that resolves {@link java.time.LocalDate} objects
     * by formatting them with the given {@link DateTimeFormatter}.
     *
     * @param formatter the DateTimeFormatter to use for formatting LocalDate values
     * @return an instance of LocalDateResolver
     */
    public static LocalDateResolver isoDate(DateTimeFormatter formatter) {
        return new LocalDateResolver(formatter);
    }

    /**
     * Returns an instance of DateResolver.
     * The DateResolver class is an implementation of the StringResolver interface
     * that creates a formatted date string for expressions that return a Date object.
     *
     * @return An instance of DateResolver
     */
    public static DateResolver legacyDate() {
        return new DateResolver();
    }

    /**
     * Creates a new instance of DateResolver using the given DateTimeFormatter.
     *
     * @param formatter the DateTimeFormatter to use for formatting Date objects
     **/
    public static DateResolver legacyDate(DateTimeFormatter formatter) {
        return new DateResolver(formatter);
    }

    /**
     * Returns an instance of ImageResolver that allows context objects to return objects of type Image.
     * An expression that resolves to an Image object will be replaced by an actual image in the resulting .docx document.
     * The image will be put as an inline into the surrounding paragraph of text.
     *
     * @return An instance of ImageResolver
     */
    public static ImageResolver image() {
        return new ImageResolver();
    }
}
