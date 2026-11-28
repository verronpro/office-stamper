package pro.verron.officestamper.api;

import java.util.List;
import org.jspecify.annotations.Nullable;

/// Creates format-native elements ([Element]s) bound to a document. Supplied by the provider and
/// obtained through [OfficePart#factory()].
public interface ElementFactory {

    /// @param text the run's text
    /// @return a new text run with no formatting
    Element run(String text);

    /// @param text       the run's text
    /// @param properties the formatting to apply, or `null` for none
    /// @return a new text run carrying the given run properties
    Element run(String text, @Nullable RunProperties properties);

    /// @param text the run's text, placed before the line break
    /// @return a new text run ending with a line break
    Element lineBreak(String text);

    /// @param text the paragraph's text
    /// @return a new paragraph holding a single run with the given text
    Element paragraph(String text);

    /// @param content the elements to place in the paragraph
    /// @return a new paragraph holding the given elements
    Element paragraph(List<Element> content);
}
