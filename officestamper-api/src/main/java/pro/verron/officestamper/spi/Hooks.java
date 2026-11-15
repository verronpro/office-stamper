package pro.verron.officestamper.spi;

import java.util.Iterator;

/// Discovers hook [Tag]s within a document part.
///
/// <p>The concrete, library-specific discovery (recursing through paragraphs, tables, SDTs,
/// textboxes and drawings) is supplied by a provider and consumed by the engine's hook orchestration.
public interface Hooks {

    /// @param part the part to scan for hook tags
    /// @return an iterator over the hook tags found in the part
    Iterator<Tag> ofHooks(OfficePart part);
}
