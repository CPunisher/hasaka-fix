package com.cpunisher.hasakafix.apply;

import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.utils.tree.SimpleNode;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ClusterManager implements ITreeMatcher {
    private int hSize = 0, nodeSize = 0;
    private List<Cluster<GTTreeEdit>> clusters = new ArrayList<>();
    private final Map<Cluster<GTTreeEdit>, String> idMap = new HashMap<>();
    private final List<Cluster<GTTreeEdit>> concretes = new ArrayList<>();
    private final Map<Cluster<GTTreeEdit>, Integer> heightMap = new HashMap<>();
    private final Map<Cluster<GTTreeEdit>, Integer> subtreeMatchMap = new HashMap<>();
    private final Map<Cluster<GTTreeEdit>, Double> rankingMap = new HashMap<>();

    private final Matcher matcher;

    public ClusterManager(Matcher matcher) {
        this.matcher = matcher;
    }

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
        idMap.put(cluster, node.id());
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

        int total = 0;
        for (var concrete : concretes) {
            for (var tree : concrete.pattern().before().preOrder()) {
                if (match(cluster.pattern().before(), tree).isPresent()) {
                    total++;
                }
            }
        }
        subtreeMatchMap.put(cluster, total);
    }

    private void calculateRanking(Cluster<GTTreeEdit> cluster) {
        for (var child : cluster.children()) {
            calculateRanking(child);
        }
        double prevalence = (double) heightMap.get(cluster) / hSize;
        double specialized = nodeSize / (double) subtreeMatchMap.get(cluster);
        rankingMap.put(cluster, prevalence * specialized);
    }

    public Optional<Map<String, Tree>> match(Tree pattern, Tree tree) {
        Map<String, Tree> mappings = new HashMap<>();
        if (!pattern.hasSameTypeAndLabel(tree)) {
            if (!pattern.getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL)) {
                return Optional.empty();
            }
            // replace
            if (Objects.equals(pattern.getType(), PlainAntiUnifier2.HOLE_TYPE) || pattern.hasSameType(tree)) {
                mappings.put(pattern.getLabel(), tree);
                return Optional.of(mappings);
            }
            return Optional.empty();
        }

        if (pattern.getChildren().size() != tree.getChildren().size()) {
            return Optional.empty();
        }

        for (int i = 0; i < pattern.getChildren().size(); i++) {
            var result = match(pattern.getChild(i), tree.getChild(i));
            if (result.isEmpty()) {
                return Optional.empty();
            }
            mappings.putAll(result.get());
        }
        return Optional.of(mappings);
    }

    public void initClusters(List<File> patterns) {
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
        System.out.println("Calculating ranking...");
        for (var cluster : clusters) calculateRanking(cluster);
        this.clusters = clusters;
    }

    public List<MatchResult> ranking(Tree tree) {
        List<MatchResult> result = new ArrayList<>();
        for (var root : clusters) {
            root.preOrder(cluster -> {
                var clusterRoot = cluster.pattern().before();
                if (clusterRoot.getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL) && clusterRoot.getType().equals(PlainAntiUnifier2.HOLE_TYPE)) {
                    return;
                }

                Tree dup = tree.deepCopy();
                Map<Tree, Tree> subs = new HashMap<>();
                for (var ch : dup.preOrder()) {
                    var matchResult = match(clusterRoot, ch);
                    if (matchResult.isPresent()) {
                        var mappings = matchResult.get();
                        Tree after = MatchResult.transform(cluster.pattern().after(), mappings);
                        subs.put(ch, after);
                    }
                }
                if (!subs.isEmpty()) {
                    if (subs.entrySet().size() == 1 && subs.containsKey(dup)) {
                        // Root replacement
                        dup = subs.get(dup);
                    } else {
                        for (var entry : subs.entrySet()) {
                            var parent = entry.getKey().getParent();
                            if (parent != null) {
                                var index = parent.getChildren().indexOf(entry.getKey());
                                parent.getChildren().set(index, entry.getValue());
                                entry.getValue().setParent(parent);
                                entry.getKey().setParent(null);
                            }
                        }
                    }
                    result.add(new MatchResult(cluster, dup));
                }
            });
        }
        result.sort((c1, c2) -> Double.compare(
                rankingMap.get(c2.cluster()),
                rankingMap.get(c1.cluster())
        ));
        return result;
    }
}
