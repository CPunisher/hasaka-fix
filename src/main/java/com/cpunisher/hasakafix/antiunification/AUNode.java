package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AUData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class AUNode {
    private AUNode left;
    private AUNode right;

    private AUData data;

    public AUNode(AUData data) {
        this.data = data;
    }

    public AUNode(final String unifier) {
        this.data = new AUData(unifier, new ArrayList<>());
    }

    public List<AUNode> getChildren() {
        return Stream.of(left, right).filter(Objects::nonNull).toList();
    }

    public String toStringTree() {
        StringJoiner joiner = new StringJoiner(", ");
        joiner.add("[" + this.getData() + "]");
        if (this.left != null && this.left.getData() != null) joiner.add("[" + this.left.getData() + "]");
        if (this.right != null && this.right.getData() != null) joiner.add("[" + this.right.getData() + "]");
        return joiner.toString();
    }

    @Override
    public String toString() {
        List<AUNode> children = getChildren();
        if (children.isEmpty()) {
            // leaf
            String value = data.unifier().trim();
            value = value.replaceAll("#", "hash_");
            value = value.replaceAll("\\$", "dollar_");
            if (value.startsWith("(")) {
                value = value.substring(1, value.length() - 1);
            }
            return  value;
        }
        StringJoiner joiner = new StringJoiner(", ");
        children.forEach(child -> joiner.add(child.toString()));
        return data + "(" + joiner + ")";
    }

    public AUData getData() {
        return data;
    }

    public void setData(AUData data) {
        this.data = data;
    }

    public AUNode getLeft() {
        return left;
    }

    public void setLeft(AUNode left) {
        this.left = left;
    }

    public AUNode getRight() {
        return right;
    }

    public void setRight(AUNode right) {
        this.right = right;
    }
}
