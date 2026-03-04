package pro.verron.officestamper.core;

import pro.verron.officestamper.api.ContextTree;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/// A branch in the context tree.
public class ContextBranch
        extends AbstractSequentialList<Object>
        implements ContextTree {
    private final ContextRoot tree;
    private final List<Object> branch;

    /// Constructs a ContextBranch with a single root object.
    ///
    /// @param tree the context root tree.
    /// @param root the root object of the branch.
    public ContextBranch(ContextRoot tree, Object root) {
        this(tree, List.of(root));
    }

    /// Constructs a ContextBranch with a list of objects forming the branch.
    ///
    /// @param tree the context root tree.
    /// @param branch the list of objects in the branch.
    public ContextBranch(ContextRoot tree, List<Object> branch) {
        this.tree = tree;
        this.branch = List.copyOf(branch);
    }

    /// Adds a new branch with the given object.
    ///
    /// @param object the object to add to the branch.
    ///
    /// @return the key of the added branch.
    public String addBranch(Object object) {
        var newBranch = new ArrayList<>(branch);
        newBranch.add(object);
        var contextBranch = new ContextBranch(tree, newBranch);
        return tree.addBranch(contextBranch);
    }

    /// Returns the root object of the branch.
    ///
    /// @return the root object.
    public Object root() {
        return branch.getLast();
    }

    @Override
    public int size() {
        return branch.size();
    }

    @Override
    public String toString() {
        return String.valueOf(branch);
    }

    public Object getLeaf() {
        return branch.getLast();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        return branch.reversed()
                     .listIterator();
    }
}
