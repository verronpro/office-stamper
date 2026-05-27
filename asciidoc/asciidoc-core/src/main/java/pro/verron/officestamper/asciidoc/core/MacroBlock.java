package pro.verron.officestamper.asciidoc.core;

import java.util.List;

/// Represents a macro block in an AsciiDoc document, which is a specialized block
/// containing a unique identifier, a name, and a list of associated data.
///
/// The [MacroBlock] is immutable and implements the [Block] interface.
/// It provides a concrete implementation for determining the size of the block.
///
/// @param name the name of the macro block
/// @param id the unique identifier for the macro block
/// @param list an ordered list of strings associated with the macro block
public record MacroBlock(String name, String id, List<String> list)
        implements Block {
    @Override
    public int size() {
        return 1;
    }
}
