package pro.verron.officestamper.api;

/// Represents a tree of contexts used for expression evaluation during document stamping. This interface provides a
/// mechanism to add nested branches to the context hierarchy, allowing for scoped evaluation of expressions (e.g.,
/// within repeaters).
public interface ContextTree {

    /// Adds a new branch to the context tree with the specified sub-context object. The new branch will have the
    /// current branch as its parent.
    ///
    /// @param subContext the object to be added as a new branch in the context tree
    ///
    /// @return a unique identifier (key) for the newly created branch
    String addBranch(Object subContext);
}
