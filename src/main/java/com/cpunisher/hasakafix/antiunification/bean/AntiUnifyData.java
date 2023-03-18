package com.cpunisher.hasakafix.antiunification.bean;

import java.util.List;

public record AntiUnifyData<T>(
        T template,
        List<AntiUnifySubstitution<T>> substitutions
) {
}
