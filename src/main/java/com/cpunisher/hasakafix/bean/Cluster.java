package com.cpunisher.hasakafix.bean;

import com.cpunisher.hasakafix.tree.AUTree;

import java.util.List;

public record Cluster<T>(
        String template,
        List<T> edits
) {
}
