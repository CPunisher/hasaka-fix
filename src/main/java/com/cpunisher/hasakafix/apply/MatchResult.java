package com.cpunisher.hasakafix.apply;

import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;

import java.util.Map;

public record MatchResult(Cluster<GTTreeEdit> cluster, Tree after) {
    public static Tree transform(Tree template, Map<String, Tree> mappings) {
        if (template.getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL)) {
            return mappings.get(template.getLabel());
        }

        Tree parent = new DefaultTree(template.getType(), template.getLabel());
        for (var child : template.getChildren()) {
            parent.addChild(transform(child, mappings));
        }
        return parent;
    }
}
