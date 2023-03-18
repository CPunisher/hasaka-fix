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
        List<Cluster<GTTreeEdit>> result = cc.cluster(edits);

        Map<GTTreeEdit, UUID> idMap = new HashMap<>();
        for (Cluster<GTTreeEdit> cluster : result) {
            UUID uuid = UUID.randomUUID();
            idMap.put(cluster.getPattern(), uuid);
            Path dir = output.toPath().resolve(uuid.toString());
            try {
                Files.createDirectories(dir);
                Path before = dir.resolve("before.xml");
                Path after = dir.resolve("after.xml");
                TreeIoUtils.toXml(XmlHelper.toTreeContext(cluster.getPattern().before())).writeTo(before.toFile());
                TreeIoUtils.toXml(XmlHelper.toTreeContext(cluster.getPattern().after())).writeTo(after.toFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SimpleNode root = new SimpleNode(idMap.get(result.get(result.size() - 1).getPattern()).toString(), new ArrayList<>());
        SimpleNode current = root;
        for (int i = result.size() - 2; i >= 0; i--) {
            current.children().add(new SimpleNode(idMap.get(result.get(i).getPattern()).toString(), new ArrayList<>()));
            current = current.children().get(0);
        }
        try {
            Files.writeString(output.toPath().resolve("tree.json"), gson.toJson(root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
