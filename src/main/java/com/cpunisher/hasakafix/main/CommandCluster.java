package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.cluster.GTHierarchicalCalculator;
import com.cpunisher.hasakafix.cluster.IClusterCalculator;
import com.cpunisher.hasakafix.utils.IdentityPair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "cluster", description = "Cluster concrete edits to get fix pattern")
public class CommandCluster implements Runnable {
    @CommandLine.Parameters(paramLabel = "<files>")
    List<File> files = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--output"}, defaultValue = "./output/result.json")
    File output;

    @Override
    public void run() {
        List<IdentityPair<String>> edits = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<List<IdentityPair<String>>>() {}.getType();
        for (File file : files) {
            try {
                String raw = Files.readString(file.toPath());
                edits.addAll(gson.fromJson(raw, type));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        IClusterCalculator<IdentityPair<String>> cc = new GTHierarchicalCalculator();
        var result = cc.cluster(edits);

        try {
            Files.writeString(output.toPath(), gson.toJson(
                    result.stream().map(cluster -> new IdentityPair<>(cluster.getBeforeTemplate(), cluster.getAfterTemplate())).toList())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
