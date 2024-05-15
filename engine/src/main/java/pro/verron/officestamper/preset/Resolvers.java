package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Placeholder;
import pro.verron.officestamper.api.StringResolver;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImagePart;

/**
 * This class provides static methods to create different types of
 * {@link ObjectResolver}.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.7
 */
public class Resolvers {

    /**
     * Returns an instance of {@link ObjectResolver} that can act as a fallback
     * resolver. Will call the {@link Object#toString()} method on every type
     * of objects.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver fallback() {
        return new ToStringResolver();
    }

    /**
     * Returns an instance of {@link ObjectResolver} that replaces null values with an empty string.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver nullToEmpty() {
        return nullToDefault("");
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves null objects
     * by creating a run with a default text value.
     *
     * @param value The default value for null objects.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver nullToDefault(String value) {
        return new Null2DefaultResolver(value);
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves null objects
     * by not replacing their expression.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver nullToPlaceholder() {
        return new Null2PlaceholderResolver();
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link LocalDateTime} values to a formatted string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} pattern.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver isoDateTime() {
        return new LocalDateTimeResolver();
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link LocalTime} values to a formatted string using the
     * {@link DateTimeFormatter#ISO_LOCAL_TIME} pattern.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver isoTime() {
        return new LocalTimeResolver();
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link LocalDate} values to a formatted string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE} pattern.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver isoDate() {
        return new LocalDateResolver();
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link LocalTime} values to a formatted string using the given
     * {@link DateTimeFormatter} pattern.
     *
     * @param formatter the {@link DateTimeFormatter} pattern to use
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver isoTime(DateTimeFormatter formatter) {
        return new LocalTimeResolver(formatter);
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link LocalDate} values to a formatted string using the given
     * {@link DateTimeFormatter} pattern.
     *
     * @param formatter the {@link DateTimeFormatter} pattern to use
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver isoDate(DateTimeFormatter formatter) {
        return new LocalDateResolver(formatter);
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link LocalDateTime} values to a formatted string using the given
     * {@link DateTimeFormatter} pattern.
     *
     * @param formatter the {@link DateTimeFormatter} pattern to use
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver isoDateTime(DateTimeFormatter formatter) {
        return new LocalDateTimeResolver(formatter);
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link Date} values to a formatted string using the
     * "dd.MM.yyyy" pattern.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver legacyDate() {
        return new DateResolver();
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link Date} values to a formatted string using the given
     * {@link DateTimeFormatter} pattern.
     *
     * @param formatter the {@link DateTimeFormatter} pattern to use
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver legacyDate(DateTimeFormatter formatter) {
        return new DateResolver(formatter);
    }

    /**
     * Returns an instance of {@link ObjectResolver} that resolves
     * {@link Image} to an actual image in the resulting .docx document.
     * The image will be put as an inline into the surrounding paragraph of text.
     *
     * @return An instance of {@link ObjectResolver}
     */
    public static ObjectResolver image() {
        return new ImageResolver();
    }

    /**
     * This {@link ObjectResolver} creates a formatted date {@link String} for
     * expressions that return a {@link Date} object.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.7
     */
    private static final class DateResolver
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
         * @param formatter the format to use for date formatting. See
         *                  {@link SimpleDateFormat}.
         */
        public DateResolver(DateTimeFormatter formatter) {
            super(Date.class);
            this.formatter = formatter;
        }

        /**
         * Resolves a formatted date string for the given {@link Date} object.
         *
         * @param date the {@link Date} object to be resolved.
         *
         * @return the formatted date string.
         */
        @Override
        protected String resolve(Date date) {
            var zone = ZoneId.systemDefault();
            var localDate = date.toInstant()
                                .atZone(zone)
                                .toLocalDate();
            return formatter.format(localDate);
        }
    }

    /**
     * This {@link ObjectResolver} allows context objects to return objects of
     * type {@link Image}. An expression that resolves to an {@link Image}
     * object will be replaced by an actual image in the resulting .docx document.
     * The image will be put as an inline into the surrounding paragraph of text.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.7
     */
    private static class ImageResolver
            implements ObjectResolver {

        @Override
        public boolean canResolve(@Nullable Object object) {
            return object instanceof Image;
        }

        @Override
        public R resolve(
                WordprocessingMLPackage document,
                String expression,
                Object object
        ) {
            if (object instanceof Image image)
                return resolve(document, image);
            String message = "Expected %s to be an Image".formatted(object);
            throw new OfficeStamperException(message);
        }

        /**
         * Resolves an image and adds it to a {@link WordprocessingMLPackage}
         * document.
         *
         * @param document The WordprocessingMLPackage document
         * @param image    The image to be resolved and added
         *
         * @return The run containing the added image
         *
         * @throws OfficeStamperException If an error occurs while adding the image to the document
         */
        public R resolve(WordprocessingMLPackage document, Image image) {
            try {
                // TODO_LATER: adding the same image twice will put the image twice into the docx-zip file. make the
                // second
                //       addition of the same image a reference instead.
                return RunUtil.createRunWithImage(image.getMaxWidth(),
                        createImagePart(document, image.getImageBytes()));
            } catch (Exception e) {
                throw new OfficeStamperException("Error while adding image to document!", e);
            }
        }
    }

    /**
     * Resolves {@link LocalDate} objects by formatting them with a {@link DateTimeFormatter}.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.4
     */
    private static final class LocalDateResolver
            extends StringResolver<LocalDate> {
        private final DateTimeFormatter formatter;

        /**
         * Uses {@link DateTimeFormatter#ISO_LOCAL_DATE} for formatting.
         */
        public LocalDateResolver() {
            this(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        /**
         * Uses the given formatter for formatting.
         *
         * @param formatter the formatter to use.
         */
        public LocalDateResolver(DateTimeFormatter formatter) {
            super(LocalDate.class);
            this.formatter = formatter;
        }

        /** {@inheritDoc} */
        @Override
        protected String resolve(LocalDate localDateTime) {
            return localDateTime.format(formatter);
        }
    }

    /**
     * Resolves {@link LocalDateTime} values to a formatted string.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.4
     */
    private static final class LocalDateTimeResolver
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

    /**
     * Resolves {@link LocalTime} values to the format specified by the {@link DateTimeFormatter} passed to the
     * constructor.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.4
     */
    private static final class LocalTimeResolver
            extends StringResolver<LocalTime> {
        private final DateTimeFormatter formatter;

        /**
         * Uses {@link DateTimeFormatter#ISO_LOCAL_TIME} for formatting.
         */
        public LocalTimeResolver() {
            this(DateTimeFormatter.ISO_LOCAL_TIME);
        }

        /**
         * <p>Constructor for LocalTimeResolver.</p>
         *
         * @param formatter a date time pattern as specified by {@link DateTimeFormatter#ofPattern(String)}
         */
        public LocalTimeResolver(DateTimeFormatter formatter) {
            super(LocalTime.class);
            this.formatter = formatter;
        }

        /** {@inheritDoc} */
        @Override
        protected String resolve(LocalTime localTime) {
            return localTime.format(formatter);
        }
    }

    /**
     * The Null2DefaultResolver class is an implementation of the
     * {@link ObjectResolver} interface
     * that resolves null objects by creating a run with a default text value.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.7
     */
    private static class Null2DefaultResolver
            implements ObjectResolver {

        private final String text;

        /**
         * The Null2DefaultResolver class is an implementation of the ObjectResolver interface
         * that resolves null objects by creating a run with a default text value.
         *
         * @param text The default text value to be used when the resolved object is null
         */
        /* package */
        public Null2DefaultResolver(String text) {
            this.text = text;
        }

        @Override
        public boolean canResolve(@Nullable Object object) {
            return object == null;
        }

        @Override
        public R resolve(
                WordprocessingMLPackage document,
                String expression,
                Object object
        ) {
            return RunUtil.create(text);
        }

        /**
         * Retrieves the default value of the {@link Null2DefaultResolver} object.
         *
         * @return the default value of the {@link Null2DefaultResolver} object as a String
         */
        public String defaultValue() {
            return text;
        }
    }

    /**
     * The {@link Null2PlaceholderResolver} class is an implementation of the ObjectResolver interface.
     * It provides a way to resolve null objects by not replacing their expression.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.7
     */
    private static class Null2PlaceholderResolver
            implements ObjectResolver {

        /* package */
        public Null2PlaceholderResolver() {
            //DO NOTHING
        }

        @Override
        public R resolve(
                WordprocessingMLPackage document,
                Placeholder placeholder,
                Object object
        ) {
            return RunUtil.create(placeholder.expression());
        }

        @Override
        public boolean canResolve(@Nullable Object object) {
            return object == null;
        }

        @Override
        public R resolve(
                WordprocessingMLPackage document,
                String expression,
                Object object
        ) {
            throw new OfficeStamperException("Should not be called");
        }
    }


    /**
     * This class is an implementation of the {@link ObjectResolver} interface
     * that resolves objects by converting them to a string representation using the
     * {@link Object#toString()} method and creating a new run with the resolved content.
     * <p>
     * * @author Joseph Verron
     * * @version ${version}
     * * @since 1.6.7
     */
    private static class ToStringResolver
            implements ObjectResolver {
        @Override
        public boolean canResolve(@Nullable Object object) {
            return object != null;
        }

        @Override
        public R resolve(
                WordprocessingMLPackage document,
                String expression,
                Object object
        ) {
            return RunUtil.create(String.valueOf(object));
        }
    }
}
