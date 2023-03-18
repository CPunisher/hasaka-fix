package com.cpunisher.hasakafix.bean;

import com.cpunisher.hasakafix.tree.AUHole;
import com.cpunisher.hasakafix.tree.AUTree;
import com.cpunisher.hasakafix.utils.Either;

public record AUTreeEdit(
        Either<AUTree, AUHole> before,
        Either<AUTree, AUHole> after
) {
}
