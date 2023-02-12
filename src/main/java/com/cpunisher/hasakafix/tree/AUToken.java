package com.cpunisher.hasakafix.tree;

import com.cpunisher.hasakafix.utils.IdentityPair;
import com.github.gumtreediff.utils.Pair;

public record AUToken(int type, String value, IdentityPair<Integer> pos) {
    public static final int
            LPAREN = 0,
            RPAREN = 1,
            COMMAS = 2,
            FUNCTION_SYMBOL = 3,
            HEDGE_VARIABLE = 4,
            TERM_VARIABLE = 5,
            CONSTANT_VARIABLE = 6,
            HOLE = 7;

    private static String[] makeTokenNames() {
        return new String[]{
                "LPAREN", "RPAREN", "COMMAS", "FUNCTION_SYMBOL", "HEDGE_VARIABLE",
                "TERM_VARIABLE", "CONSTANT_VARIABLE", "HOLE"
        };
    }

    public static final String[] tokenNames = makeTokenNames();
}
