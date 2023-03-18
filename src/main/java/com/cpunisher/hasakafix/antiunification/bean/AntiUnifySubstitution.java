package com.cpunisher.hasakafix.antiunification.bean;

public record AntiUnifySubstitution<T>(
        T substitution,
        T left,
        T right
) {
}
