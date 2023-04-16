package com.cpunisher.hasakafix.utils.tree;

import com.github.gumtreediff.tree.Tree;

import java.util.Objects;

public class GTTreeUtils {
    public static boolean treeEquals(Tree tree1, Tree tree2) {
        if (Objects.equals(tree1, tree2)) return true;

        var children1 = tree1.getChildren();
        var children2 = tree2.getChildren();
        if (!tree1.hasSameTypeAndLabel(tree2) || children1.size() != children2.size()) {
            return false;
        }

        for (int i = 0; i < children1.size(); i++) {
            if (!treeEquals(children1.get(i), children2.get(i))) {
                return false;
            }
        }
        return true;
    }
}
