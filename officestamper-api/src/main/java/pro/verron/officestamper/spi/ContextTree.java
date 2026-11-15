package pro.verron.officestamper.spi;

/// A tree of contexts used for expression evaluation during document stamping. It allows nested
/// branches to be added, enabling scoped evaluation of expressions (e.g. within repeaters).
public interface ContextTree {

    /// Adds a new branch to the context tree with the given sub-context. The new branch has the
    /// current branch as its parent.
    ///
    /// @param subContext the object to add as a new branch
    /// @return a unique key for the newly created branch
    String addBranch(Object subContext);
}
