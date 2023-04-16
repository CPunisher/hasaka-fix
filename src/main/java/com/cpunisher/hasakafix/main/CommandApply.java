package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.apply.ClusterManager;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import picocli.CommandLine;

import java.io.File;
import java.util.*;

@CommandLine.Command(name = "apply", description = "Ranking fix patterns")
public class CommandApply implements Runnable {
    private static final Matcher matcher = Matchers.getInstance().getMatcher();

    @CommandLine.Parameters(paramLabel = "<patterns>")
    List<File> patterns = new ArrayList<>();

    private ClusterManager clusterManager = new ClusterManager(matcher);

    @Override
    public void run() {
        clusterManager.initClusterFiles(this.patterns);
        Scanner scanner = new Scanner(System.in);
        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
            stringBuilder.append(System.lineSeparator());
        }
    }
}
