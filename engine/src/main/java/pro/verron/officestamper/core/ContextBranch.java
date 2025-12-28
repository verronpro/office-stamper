package pro.verron.officestamper.core;

import pro.verron.officestamper.api.ContextTree;

import java.util.ArrayList;
import java.util.List;

public class ContextBranch
        implements ContextTree {
    private final ContextRoot tree;
    private final List<Object> branch;

    public ContextBranch(ContextRoot tree, Object root) {
        this(tree, List.of(root));
    }

    public ContextBranch(ContextRoot tree, List<Object> branch) {
        this.tree = tree;
        this.branch = branch;
    }

    public String addBranch(Object object) {
        var newBranch = new ArrayList<>(branch);
        newBranch.add(object);
        var contextBranch = new ContextBranch(tree, newBranch);
        return tree.addBranch(contextBranch);
    }

    public Object root() {
        return branch.getLast();
    }

    public List<Object> list() {
        return List.copyOf(branch)
                   .reversed();
    }

    @Override
    public String toString() {
        return String.valueOf(branch.getLast());
    }
}
