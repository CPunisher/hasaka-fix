package com.cpunisher.hasakafix.edit.editor.gumtree;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;

public record GTTreeEdit(
        Tree before,
        Tree after,
        MappingStore mappings
) {
}
