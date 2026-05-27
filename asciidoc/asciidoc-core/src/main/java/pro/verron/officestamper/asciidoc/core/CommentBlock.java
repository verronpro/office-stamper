package pro.verron.officestamper.asciidoc.core;

/// Represents a comment line in the AsciiDoc document model.
///
/// A comment line is considered a block-level element but does not contribute
/// any visible content to the output document. It is typically used to store
/// annotations or additional information within the block structure.
///
/// This class implements the [Block] interface, which mandates
/// implementing the [#size()] method. The size of a comment line
/// is always zero, as it does not represent any visual or measurable content.
///
/// @param comment the text of the comment line
public record CommentBlock(String comment)
        implements Block {
    @Override
    public int size() {
        return 0;
    }
}
