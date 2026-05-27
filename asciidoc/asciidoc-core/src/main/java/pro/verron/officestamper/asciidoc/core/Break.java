package pro.verron.officestamper.asciidoc.core;

/// Represents a line break in a document structure.
/// This is a marker record that implements the [Block] interface.
/// It has a fixed size of zero, indicating no content spans across the line break.
public record Break()
        implements Block {
    @Override
    public int size() {
        return 0;
    }
}
