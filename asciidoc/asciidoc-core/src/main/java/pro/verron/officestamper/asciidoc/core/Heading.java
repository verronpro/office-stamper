package pro.verron.officestamper.asciidoc.core;

import java.util.List;

import static java.util.Collections.emptyList;

/// Heading block (levels 1..6).
///
/// @param level heading level
/// @param inlines inline fragments
public record Heading(List<String> header, int level, List<Inline> inlines)
        implements Block {
    /// Constructs a Heading object with the specified heading level and inline fragments.
    ///
    /// @param level the heading level, must be between 1 and 6 (inclusive)
    /// @param inlines the list of inline fragments representing the content of the heading
    /// @throws IllegalArgumentException if the heading level is outside the range of 1 to 6
    public Heading(int level, List<Inline> inlines) {
        this(emptyList(), level, inlines);
    }

    /// Constructor.
    ///
    /// @param level heading level
    /// @param inlines inline fragments
    public Heading(List<String> header, int level, List<Inline> inlines) {
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("Heading level must be between 1 and 6");
        }
        this.header = List.copyOf(header);
        this.level = level;
        this.inlines = List.copyOf(inlines);
    }

    @Override
    public int size() {
        return 1;
    }
}
