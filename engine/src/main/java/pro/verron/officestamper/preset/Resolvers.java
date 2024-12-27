package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.resolvers.date.DateResolver;
import pro.verron.officestamper.preset.resolvers.image.ImageResolver;
import pro.verron.officestamper.preset.resolvers.localdate.LocalDateResolver;
import pro.verron.officestamper.preset.resolvers.localdatetime.LocalDateTimeResolver;
import pro.verron.officestamper.preset.resolvers.localtime.LocalTimeResolver;
import pro.verron.officestamper.preset.resolvers.nulls.Null2DefaultResolver;
import pro.verron.officestamper.preset.resolvers.nulls.Null2PlaceholderResolver;
import pro.verron.officestamper.preset.resolvers.objects.ToStringResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/// This class provides static methods to create different types of [ObjectResolver].
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class Resolvers {

    private Resolvers() {
        throw new OfficeStamperException("Resolvers cannot be instantiated");
    }

    /// Returns an instance of [ObjectResolver] that can act as a fallback
    /// resolver.
    /// Calls the [#toString()] method on every object.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver fallback() {
        return new ToStringResolver();
    }

    /// Returns an instance of [ObjectResolver] that replaces null values with an empty string.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver nullToEmpty() {
        return nullToDefault("");
    }

    /// Returns an instance of [ObjectResolver] that resolves null objects
    /// by creating a run with a default text value.
    ///
    /// @param value The default value for null objects.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver nullToDefault(String value) {
        return new Null2DefaultResolver(value);
    }

    /// Returns an instance of [ObjectResolver] that resolves null objects
    /// by not replacing their expression.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver nullToPlaceholder() {
        return new Null2PlaceholderResolver();
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [LocalDateTime] values to a formatted string using the
    /// [DateTimeFormatter#ISO_LOCAL_DATE_TIME] pattern.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver isoDateTime() {
        return new LocalDateTimeResolver();
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [LocalTime] values to a formatted string using the
    /// [DateTimeFormatter#ISO_LOCAL_TIME] pattern.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver isoTime() {
        return new LocalTimeResolver();
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [LocalDate] values to a formatted string using the
    /// [DateTimeFormatter#ISO_LOCAL_DATE] pattern.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver isoDate() {
        return new LocalDateResolver();
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [LocalTime] values to a formatted string using the given
    /// [DateTimeFormatter] pattern.
    ///
    /// @param formatter the [DateTimeFormatter] pattern to use
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver isoTime(DateTimeFormatter formatter) {
        return new LocalTimeResolver(formatter);
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [LocalDate] values to a formatted string using the given
    /// [DateTimeFormatter] pattern.
    ///
    /// @param formatter the [DateTimeFormatter] pattern to use
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver isoDate(DateTimeFormatter formatter) {
        return new LocalDateResolver(formatter);
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [LocalDateTime] values to a formatted string using the given
    /// [DateTimeFormatter] pattern.
    ///
    /// @param formatter the [DateTimeFormatter] pattern to use
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver isoDateTime(DateTimeFormatter formatter) {
        return new LocalDateTimeResolver(formatter);
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [Date] values to a formatted string using the `dd.MM.yyyy` pattern.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver legacyDate() {
        return new DateResolver();
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [Date] values to a formatted string using the given
    /// [DateTimeFormatter] pattern.
    ///
    /// @param formatter the [DateTimeFormatter] pattern to use
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver legacyDate(DateTimeFormatter formatter) {
        return new DateResolver(formatter);
    }

    /// Returns an instance of [ObjectResolver] that resolves
    /// [Image] to an actual image in the resulting .docx document.
    /// The image is inlined into the surrounding paragraph of text.
    ///
    /// @return An instance of [ObjectResolver]
    public static ObjectResolver image() {
        return new ImageResolver();
    }
}
