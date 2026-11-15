package pro.verron.officestamper.spi;

import java.util.Iterator;

/// Discovers {@link Hook}s within a document part.
///
/// <p>The concrete, library-specific discovery (e.g. docx4j smart-tag traversal) is supplied by a
/// provider and registered through this abstraction.
public interface Hooks {

    /// @param part the part to scan for hooks
    /// @return an iterator over the hooks found in the part
    Iterator<Hook> ofHooks(OfficePart part);
}
