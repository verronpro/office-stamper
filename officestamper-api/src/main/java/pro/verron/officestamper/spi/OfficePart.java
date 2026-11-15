package pro.verron.officestamper.spi;

import java.util.List;

/// A part of an {@link OfficeDocument} (e.g. a worksheet or the main document
/// part), exposing its content and comments without leaking the underlying
/// library types.
public interface OfficePart {

    /// @return the part name
    String name();

    /// @return a live, mutable view of the content tree of this part
    Content content();

    /// @return the comments declared in this part
    List<Comment> comments();

    /// @return the document this part belongs to
    OfficeDocument document();

    /// @return the hook and comment discovery service for this part
    Hooks hooks();

    /// @return a factory creating format-native elements bound to this document
    ElementFactory factory();

    /// Returns the native representation of this part, for processors that implement
    /// format-specific actions against the provider's types (e.g. the docx4j part object).
    ///
    /// @param type the expected native type
    /// @param <T>  the native type
    /// @return the native representation, cast to the given type
    /// @throws IllegalArgumentException if the provider does not support the requested type
    <T> T unwrap(Class<T> type);
}
