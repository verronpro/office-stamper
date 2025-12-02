package pro.verron.officestamper.core;

import java.util.ArrayList;
import java.util.List;

public class ContextBranch {
    private final ContextTree tree;
    private final List<Object> branch;

    public ContextBranch(ContextTree tree, Object root) {
        this(tree, List.of(root));
    }

    public ContextBranch(ContextTree tree, List<Object> branch) {
        this.tree = tree;
        this.branch = branch;
    }

    public int add(Object object) {
        var newBranch = new ArrayList<>(branch);
        newBranch.add(object);
        var contextBranch = new ContextBranch(tree, newBranch);
        var nextIndex = tree.size();
        tree.add(contextBranch);
        return nextIndex;
    }

    public Object root() {
        return branch.getLast();
    }
}
