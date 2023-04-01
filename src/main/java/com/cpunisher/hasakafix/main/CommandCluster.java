package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.cluster.GTHierarchicalCalculator;
import com.cpunisher.hasakafix.cluster.IClusterCalculator;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.utils.XmlHelper;
import com.cpunisher.hasakafix.utils.tree.SimpleNode;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@CommandLine.Command(name = "cluster", description = "Cluster concrete edits to get fix pattern")
public class CommandCluster implements Runnable {
    @CommandLine.Parameters(paramLabel = "<files>")
    List<File> files = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--output"}, defaultValue = "./output/result.json")
    File output;

    @Override
    public void run() {
        List<GTTreeEdit> edits = new ArrayList<>();
        Matcher matcher = Matchers.getInstance().getMatcher();
        int total = files.size(), finish = 0;
        for (File dir : files) {
            if (!dir.exists() || !dir.isDirectory()) {
                continue;
            }
            int count = Objects.requireNonNull(dir.list()).length / 2;
            for (int i = 0; i < count; i++) {
                Path before = dir.toPath().resolve("before_" + i + ".xml");
                Path after = dir.toPath().resolve("after_" + i + ".xml");
                try {
                    Tree beforeTree = TreeIoUtils.fromXml().generateFrom().file(before).getRoot();
                    Tree afterTree = TreeIoUtils.fromXml().generateFrom().file(after).getRoot();
                    var mapping = matcher.match(beforeTree, afterTree);
                    edits.add(new GTTreeEdit(beforeTree, afterTree, mapping));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.printf("[%d/%d] Finish file %s\n", ++finish, total, dir.getName());
        }

        IClusterCalculator<GTTreeEdit> cc = new GTHierarchicalCalculator(new PlainAntiUnifier2());
        List<Cluster<GTTreeEdit>> clusters = cc.cluster(edits);
        for (int i = 0; i < clusters.size(); i++) {
            saveCluster(clusters.get(i), "cluster_" + i);
        }
    }

    private void saveCluster(Cluster<GTTreeEdit> rootCluster, String identity) {
        SimpleNode rootNode = new SimpleNode("", new ArrayList<>());

        Queue<Pair<Cluster<GTTreeEdit>, SimpleNode>> queue = new LinkedList<>();
        queue.add(new Pair<>(rootCluster, rootNode));
        while (!queue.isEmpty()) {
            var pair = queue.remove();
            Cluster<GTTreeEdit> cluster = pair.first;
            SimpleNode node = pair.second;

            UUID uuid = UUID.randomUUID();
            node.id(uuid.toString());

            // Write file
            Path dir = output.toPath().resolve(identity).resolve(uuid.toString());
            try {
                Files.createDirectories(dir);
                Path before = dir.resolve("before.xml");
                Path after = dir.resolve("after.xml");
                TreeIoUtils.toXml(XmlHelper.toTreeContext(cluster.pattern().before())).writeTo(before.toFile());
                TreeIoUtils.toXml(XmlHelper.toTreeContext(cluster.pattern().after())).writeTo(after.toFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            for (var child : cluster.children()) {
                SimpleNode childNode = new SimpleNode("", new ArrayList<>());
                node.children().add(childNode);
                queue.add(new Pair<>(child, childNode));
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.writeString(output.toPath().resolve(identity).resolve("tree.json"), gson.toJson(rootNode));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
