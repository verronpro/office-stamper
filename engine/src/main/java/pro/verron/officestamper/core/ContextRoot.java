package pro.verron.officestamper.core;

import pro.verron.officestamper.api.ContextTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextRoot
        implements ContextTree {

    private static final String ROOT_KEY = String.valueOf(0);
    private final Map<String, ContextBranch> branches;
    private final Object root;

    public ContextRoot(Object root) {
        this.branches = new HashMap<>();
        this.root = root;
        this.branches.put(ROOT_KEY, new ContextBranch(this, root));
    }

    public ContextBranch find(String key) {
        return branches.get(key);
    }

    @Override
    public String addBranch(Object subContext) {
        var contextElements = List.of(root, subContext);
        var contextBranch = new ContextBranch(this, contextElements);
        return addBranch(contextBranch);
    }

    public String addBranch(ContextBranch contextBranch) {
        var key = computeNewKey();
        branches.put(key, contextBranch);
        return key;
    }

    private String computeNewKey() {
        return String.valueOf(branches.size());
    }
}
