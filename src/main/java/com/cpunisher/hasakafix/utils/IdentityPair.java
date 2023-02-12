package com.cpunisher.hasakafix.utils;

import com.github.gumtreediff.utils.Pair;

public class IdentityPair<T> extends Pair<T, T> {
    public IdentityPair(T a, T b) {
        super(a, b);
    }
}
