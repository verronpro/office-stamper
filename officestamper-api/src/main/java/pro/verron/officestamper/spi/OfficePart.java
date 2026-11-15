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

    /// @return a factory creating format-native elements bound to this document
    ElementFactory factory();
}
