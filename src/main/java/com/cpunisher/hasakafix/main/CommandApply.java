package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.utils.tree.SimpleNode;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeVisitor;
import com.google.gson.Gson;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@CommandLine.Command(name = "apply", description = "Ranking fix patterns")
public class CommandApply implements Runnable {
    @CommandLine.Parameters(paramLabel = "<patterns>")
    List<File> patterns = new ArrayList<>();

    private static final Matcher matcher = Matchers.getInstance().getMatcher();

    private int hSize = 0, nodeSize = 0;
    private List<Cluster<GTTreeEdit>> clusters = new ArrayList<>();
    private final List<Cluster<GTTreeEdit>> concretes = new ArrayList<>();
    private final Map<Cluster<GTTreeEdit>, Integer> heightMap = new HashMap<>();
    private final Map<Cluster<GTTreeEdit>, Integer> subtreeMatchMap = new HashMap<>();

    private Cluster<GTTreeEdit> loadCluster(Path dir, SimpleNode node, int depth) throws IOException {
        Path before = dir.resolve(node.id()).resolve("before.xml");
        Path after = dir.resolve(node.id()).resolve("after.xml");
        Tree beforeTree = TreeIoUtils.fromXml().generateFrom().file(before).getRoot();
        Tree afterTree = TreeIoUtils.fromXml().generateFrom().file(after).getRoot();
        var mapping = matcher.match(beforeTree, afterTree);

        List<Cluster<GTTreeEdit>> children = new LinkedList<>();

        for (var child : node.children()) {
            children.add(loadCluster(dir, child, depth + 1));
        }

        Cluster<GTTreeEdit> cluster = new Cluster<>(
                new GTTreeEdit(beforeTree, afterTree, mapping),
                children
        );

        if (node.children().isEmpty()) {
            concretes.add(cluster);
        }
        return cluster;
    }

    private void calculateHeight(Cluster<GTTreeEdit> cluster) {
        // Leaf
        if (cluster.children().isEmpty()) {
            heightMap.put(cluster, 1);
            hSize++;
            return;
        }

        int total = 0;
        for (var child : cluster.children()) {
            calculateHeight(child);
            total += heightMap.get(child);
        }
        heightMap.put(cluster, total);
    }

    private void calculateSubtreeMatch(Cluster<GTTreeEdit> cluster) {
        for (var child : cluster.children()) {
            calculateSubtreeMatch(child);
        }

        // Concrete
        if (!cluster.children().isEmpty()) {
            nodeSize += cluster.pattern().before().getMetrics().size;
        }

        final int[] total = {0};
        for (var concrete : concretes) {
            TreeVisitor.visitTree(concrete.pattern().before(), new TreeVisitor.DefaultTreeVisitor() {
                @Override
                public void startTree(Tree tree) {
                    if (match(cluster.pattern().before(), tree)) {
                        total[0]++;
                    }
                }
            });
        }
        subtreeMatchMap.put(cluster, total[0]);
    }

    private void initClusters() {
        int total = patterns.size(), current = 1;
        System.out.println("Initializing clusters...");
        List<Cluster<GTTreeEdit>> clusters = new ArrayList<>();
        Gson gson = new Gson();
        for (File pattern : patterns) {
            try {
                String raw = Files.readString(pattern.toPath().resolve("tree.json"));
                SimpleNode root = gson.fromJson(raw, SimpleNode.class);
                Cluster<GTTreeEdit> cluster = loadCluster(pattern.toPath(), root, 0);
                clusters.add(cluster);
                System.out.printf("[%d/%d] Deserialized %s\n", current++, total, pattern.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Calculating cluster heights...");
        for (var cluster : clusters) calculateHeight(cluster);
        System.out.println("Calculating cluster subtree matches...");
        for (var cluster : clusters) calculateSubtreeMatch(cluster);
        this.clusters = clusters;
    }

    private boolean match(Tree pattern, Tree tree) {
        if (!pattern.hasSameTypeAndLabel(tree)) {
            if (!Objects.equals(pattern.getLabel(), PlainAntiUnifier2.HOLE_LABEL)) {
                return false;
            }
            // replace
            return Objects.equals(pattern.getType(), PlainAntiUnifier2.HOLE_TYPE) || pattern.hasSameType(tree);
        }

        if (pattern.getChildren().size() != tree.getChildren().size()) {
            return false;
        }

        for (int i = 0; i < pattern.getChildren().size(); i++) {
            if (!match(pattern.getChild(i), tree.getChild(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        initClusters();

        Scanner scanner = new Scanner(System.in);
        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
            stringBuilder.append(System.lineSeparator());
        }
    }
}
