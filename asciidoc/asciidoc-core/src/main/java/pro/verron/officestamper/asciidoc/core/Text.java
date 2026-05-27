package pro.verron.officestamper.asciidoc.core;

/// Text fragment.
///
/// @param text text
public record Text(String text)
        implements Inline {}
