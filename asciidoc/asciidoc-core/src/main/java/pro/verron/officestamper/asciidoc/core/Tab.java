package pro.verron.officestamper.asciidoc.core;

/// Inline tab marker to be rendered as a DOCX tab stop.
public record Tab()
        implements Inline {
    @Override
    public String text() {
        return "\t";
    }
}
