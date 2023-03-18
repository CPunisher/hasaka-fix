package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.tree.AUHole;
import com.cpunisher.hasakafix.tree.AUTree;
import com.cpunisher.hasakafix.utils.Either;

public class DCapCalculator {
    public static void dcap(AUTree tree, int d) {
        if (d == 1) {
            tree.children().replaceAll(child -> Either.second(new AUHole("#")));
            return;
        }
        for (Either<AUTree, AUHole> child : tree.children()) {
            child.ifFirst(c -> dcap(c, d - 1));
        }
    }
}
