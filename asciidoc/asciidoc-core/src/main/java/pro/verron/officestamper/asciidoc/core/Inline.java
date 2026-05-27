package pro.verron.officestamper.asciidoc.core;

/// Inline fragment inside a paragraph/heading.
public sealed interface Inline
        permits Bold, ImageInline, MacroInline, Italic, Link, Styled, Sub, Sup, Tab, Text {
    /// Returns the text of the inline fragment.
    ///
    /// @return text
    String text();
}
