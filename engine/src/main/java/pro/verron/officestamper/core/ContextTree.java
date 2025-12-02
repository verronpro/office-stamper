package pro.verron.officestamper.core;

import java.util.ArrayList;
import java.util.List;

public class ContextTree {

    private final List<ContextBranch> branches;

    public ContextTree(Object root) {
        branches = new ArrayList<>();
        branches.add(new ContextBranch(this, root));
    }

    public ContextBranch find(int index) {
        return branches.get(index);
    }

    public int size() {
        return branches.size();
    }

    public void add(ContextBranch contextBranch) {
        branches.add(contextBranch);
    }
}
