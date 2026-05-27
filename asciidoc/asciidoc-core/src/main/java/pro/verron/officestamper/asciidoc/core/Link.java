package pro.verron.officestamper.asciidoc.core;

/// Link inline.
///
/// @param url link URL
/// @param text link text
public record Link(String url, String text)
        implements Inline {}
