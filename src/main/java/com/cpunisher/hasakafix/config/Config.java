package com.cpunisher.hasakafix.config;

public record Config(
        int maxCommit
) {

    public static Config fromArgs(String[] args) {
        return new Config(
                1
        );
    }
}
