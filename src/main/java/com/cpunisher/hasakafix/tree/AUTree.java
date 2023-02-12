package com.cpunisher.hasakafix.tree;

import com.cpunisher.hasakafix.utils.Either;
import com.cpunisher.hasakafix.utils.IdentityPair;

import java.util.List;

public record AUTree(
        IdentityPair<Integer> pos,
        String label,
        String value,
        List<Either<AUTree, AUHole>> children
) {
    @Override
    public String toString() {
        return "AUTree{" +
                "pos=" + pos +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", children=" + children +
                '}';
    }
}
