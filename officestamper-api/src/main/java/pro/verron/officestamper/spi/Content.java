package pro.verron.officestamper.spi;

import java.util.List;

/// The content of an {@link OfficePart}, expressed as a tree of {@link Element}s.
public interface Content {

    /// @return the direct child elements of this content
    List<Element> elements();
}
