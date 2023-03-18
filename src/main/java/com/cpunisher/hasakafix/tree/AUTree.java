package com.cpunisher.hasakafix.tree;

import com.cpunisher.hasakafix.utils.Either;
import com.cpunisher.hasakafix.utils.IdentityPair;

import java.util.List;

public record AUTree(
        String label,
        String value,
        List<Either<AUTree, AUHole>> children
) {
    public int size() {
        if (children.isEmpty()) {
            return 1;
        }

        int size = 0;
        for (Either<AUTree, AUHole> child : children) {
            size += child.fold(AUTree::size, hole -> 1);
        }
        return size;
    }
    @Override
    public String toString() {
        return "AUTree{" +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", children=" + children +
                '}';
    }
}
