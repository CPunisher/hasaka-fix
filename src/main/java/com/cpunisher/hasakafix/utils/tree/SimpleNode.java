package com.cpunisher.hasakafix.utils.tree;

import java.util.List;

public class SimpleNode {
    private String id;
    private List<SimpleNode> children;

    public SimpleNode(String id, List<SimpleNode> children) {
        this.id = id;
        this.children = children;
    }

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public List<SimpleNode> children() {
        return children;
    }

    public void children(List<SimpleNode> children) {
        this.children = children;
    }
}
