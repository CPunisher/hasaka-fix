package com.cpunisher.hasakafix.edit.editor.gumtree;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;

import java.util.Set;

public class GTTreeEdit {
    private final Tree before;
    private final Tree after;
    private final MappingStore mappings;
    private Set<Tree> modified = null;

    public GTTreeEdit(Tree before, Tree after, MappingStore mappings) {
        this.before = before;
        this.after = after;
        this.mappings = mappings;
    }

    public Tree before() {
        return before;
    }

    public Tree after() {
        return after;
    }

    public MappingStore mappings() {
        return mappings;
    }

    public Set<Tree> modified() {
        return modified;
    }

    public void modified(Set<Tree> modified) {
        this.modified = modified;
    }
}
