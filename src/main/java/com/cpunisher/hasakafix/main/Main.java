package com.cpunisher.hasakafix.main;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        System.exit(new CommandLine(new HasakaFix()).execute(args));
    }
}
