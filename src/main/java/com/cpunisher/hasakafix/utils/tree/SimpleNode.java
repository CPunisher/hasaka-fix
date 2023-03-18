package com.cpunisher.hasakafix.utils.tree;

import java.util.List;

public record SimpleNode(String id, List<SimpleNode> children) {
}
