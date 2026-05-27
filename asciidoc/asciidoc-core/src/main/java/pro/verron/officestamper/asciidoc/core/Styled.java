package pro.verron.officestamper.asciidoc.core;

import java.util.List;

import static java.util.stream.Collectors.joining;

/// Represents an inline fragment within a paragraph or heading that is styled with a specific role.
///
/// A Styled instance encapsulates:
/// - A role, which defines the style or semantic meaning associated with the content.
/// - A list of children, which are other inline elements that are part of this styled fragment.
///
/// This record implements the Inline interface, providing functionality to retrieve
/// styled text by concatenating the text from all its child inline elements.
///
/// Responsibilities:
/// - Holds a role and its associated inline content.
/// - Provides a textual representation of the styled content by aggregating the text
///   from all children inline elements.
public record Styled(String role, List<Inline> children)
        implements Inline {
    @Override
    public String text() {
        return children.stream()
                       .map(Inline::text)
                       .collect(joining());
    }
}
