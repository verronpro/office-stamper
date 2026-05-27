package pro.verron.officestamper.asciidoc.core;

import java.util.List;

/// Represents an OpenBlock element in an AsciiDoc model.
///
/// An OpenBlock is a container for grouping other blocks and includes both header and content sections.
/// The header contains metadata or data relevant to the block, while the content is the list of
/// individual blocks encapsulated within this OpenBlock.
///
/// This implementation computes the size of the OpenBlock as the sum of the sizes of its content blocks.
///
/// @param header  a list of strings representing metadata or informational content about the OpenBlock.
/// @param content a list of [Block] elements comprising the actual blocks grouped by this OpenBlock.
///
/// @see Block
public record OpenBlock(List<String> header, List<Block> content)
        implements Block {
    @Override
    public int size() {
        return content.stream()
                      .mapToInt(Block::size)
                      .sum();
    }
}
