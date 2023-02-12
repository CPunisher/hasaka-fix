package com.cpunisher.hasakafix.edit.editor.gumtree;

import com.github.gumtreediff.tree.FakeTree;
import com.github.gumtreediff.tree.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GTHelper {
    public static boolean isFakeRoot(Tree tree) {
        return tree.isRoot() && tree instanceof FakeTree;
    }

    public static List<Tree> getPathFromRoot(Tree tree) {
        List<Tree> path = new ArrayList<>();
        for (Tree node = tree; node != null && !isFakeRoot(node); node = node.getParent()) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }
}
