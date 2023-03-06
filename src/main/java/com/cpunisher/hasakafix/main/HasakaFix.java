package com.cpunisher.hasakafix.main;

import picocli.CommandLine;

@CommandLine.Command(name = "hasaka-fix", subcommands = {
        CommandEditFiles.class,
})
public class HasakaFix implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.err);
    }
}
