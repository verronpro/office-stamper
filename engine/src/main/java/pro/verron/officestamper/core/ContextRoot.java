package pro.verron.officestamper.core;

import pro.verron.officestamper.api.ContextTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// The root of the context tree.
public class ContextRoot
        implements ContextTree {

    private static final String ROOT_KEY = String.valueOf(0);
    private final Map<String, ContextBranch> branches;
    private final Object root;

    /// Constructs a ContextRoot with the given root object.
    ///
    /// @param root the root object.
    public ContextRoot(Object root) {
        this.branches = new HashMap<>();
        this.root = root;
        this.branches.put(ROOT_KEY, new ContextBranch(this, root));
    }

    /// Finds a branch by its key.
    ///
    /// @param key the key of the branch.
    ///
    /// @return the branch, or `null` if not found.
    public ContextBranch find(String key) {
        return branches.get(key);
    }

    @Override
    public String addBranch(Object subContext) {
        var contextElements = List.of(root, subContext);
        var contextBranch = new ContextBranch(this, contextElements);
        return addBranch(contextBranch);
    }

    /// Adds a branch to the root.
    ///
    /// @param contextBranch the branch to add.
    /// @return the key of the added branch.
    public String addBranch(ContextBranch contextBranch) {
        var key = computeNewKey();
        branches.put(key, contextBranch);
        return key;
    }

    private String computeNewKey() {
        return String.valueOf(branches.size());
    }
}
