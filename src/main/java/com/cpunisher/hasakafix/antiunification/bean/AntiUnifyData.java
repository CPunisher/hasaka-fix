package com.cpunisher.hasakafix.antiunification.bean;

import java.util.List;

public record AntiUnifyData(
        String template,
        List<AntiUnifySubstitution> substitutions
) {
}
