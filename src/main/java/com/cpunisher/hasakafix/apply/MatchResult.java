package com.cpunisher.hasakafix.apply;

import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;

import java.util.Map;

public record MatchResult(Cluster<GTTreeEdit> cluster, Map<String, Tree> mappings) {
    public Tree transformed() {
        return transform(cluster.pattern().after());
    }

    private Tree transform(Tree template) {
        if (template.getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL)) {
            return mappings.get(template.getLabel());
        }

        Tree parent = new DefaultTree(template.getType(), template.getLabel());
        for (var child : template.getChildren()) {
            parent.addChild(transform(child));
        }
        return parent;
    }
}
