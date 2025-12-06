package pro.verron.officestamper.core;

import java.util.HashMap;
import java.util.Map;

public class ContextTree {

    private static final String ROOT_KEY = String.valueOf(0);
    private final Map<String, ContextBranch> branches;

    public ContextTree(Object root) {
        branches = new HashMap<>();
        branches.put(ROOT_KEY, new ContextBranch(this, root));
    }

    public ContextBranch find(String key) {
        return branches.get(key);
    }

    public String add(ContextBranch contextBranch) {
        var key = computeNewKey();
        branches.put(key, contextBranch);
        return key;
    }

    private String computeNewKey() {
        return String.valueOf(branches.size());
    }
}
