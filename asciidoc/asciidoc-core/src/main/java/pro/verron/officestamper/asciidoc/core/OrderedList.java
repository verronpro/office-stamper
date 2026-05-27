package pro.verron.officestamper.asciidoc.core;

import java.util.List;

/// Ordered list.
///
/// @param items list items
public record OrderedList(List<ListItem> items)
        implements Block {
    @Override
    public int size() {
        return items.size();
    }
}
