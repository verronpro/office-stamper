package pro.verron.officestamper.spi;

import java.util.Iterator;

/// Discovers hook [Tag]s within a document part or any element subtree.
///
/// <p>The concrete, library-specific discovery (recursing through paragraphs, tables, SDTs,
/// textboxes and drawings) is supplied by a provider and consumed by the engine's hook orchestration.
///
/// <p>Subtree-scoped discovery exists because deep-copied subtrees (e.g. a table row duplicated by a
/// repeat processor) must have their hook tags re-discovered before being rebound to a new context.
public interface Hooks {

    /// @param part the part to scan for hook tags
    /// @return an iterator over the hook tags found in the part
    Iterator<Tag> ofHooks(OfficePart part);

    /// @param scope the element whose subtree to scan for hook tags
    /// @return an iterator over the hook tags found within the given subtree
    Iterator<Tag> ofHooks(Element scope);
}
