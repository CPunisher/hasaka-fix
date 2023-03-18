package com.cpunisher.hasakafix.utils;

import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;

public class XmlHelper {

    public static TreeContext toTreeContext(Tree root) {
        TreeContext context = new TreeContext();
        context.setRoot(root);
        return context;
    }
}
