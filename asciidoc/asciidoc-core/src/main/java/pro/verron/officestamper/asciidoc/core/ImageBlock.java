package pro.verron.officestamper.asciidoc.core;

/// Image block.
///
/// @param url image URL
/// @param altText alternative text
public record ImageBlock(String url, String altText)
        implements Block {
    @Override
    public int size() {
        return 1;
    }
}
