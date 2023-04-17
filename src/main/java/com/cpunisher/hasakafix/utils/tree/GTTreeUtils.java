package com.cpunisher.hasakafix.utils.tree;

import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GTTreeUtils {
    public static List<GTTreeEdit> readCommitDir(File commit) {
        Matcher matcher = Matchers.getInstance().getMatcher();
        List<GTTreeEdit> editList = new ArrayList<>();
        if (!commit.exists() || !commit.isDirectory()) {
            return editList;
        }
        int count = Objects.requireNonNull(commit.list()).length / 2;
        for (int i = 0 ; i < count; i++) {
            Path before = commit.toPath().resolve("before_" + i + ".xml");
            Path after = commit.toPath().resolve("after_" + i + ".xml");
            try {
                Tree beforeTree = TreeIoUtils.fromXml().generateFrom().file(before).getRoot();
                Tree afterTree = TreeIoUtils.fromXml().generateFrom().file(after).getRoot();
                var mapping = matcher.match(beforeTree, afterTree);
                editList.add(new GTTreeEdit(beforeTree, afterTree, mapping));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return editList;
    }

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
