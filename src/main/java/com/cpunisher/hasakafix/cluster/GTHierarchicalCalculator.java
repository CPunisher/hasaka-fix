package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.GTPlainAntiUnifier;
import com.cpunisher.hasakafix.antiunification.IAntiUnifier;
import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;
import java.util.function.Consumer;

public class GTHierarchicalCalculator {
    private final GTPlainAntiUnifier antiUnifier;
    private final GTCostCalculator costCalculator;
    private final Table<Cluster<GTTreeEdit>, Cluster<GTTreeEdit>, CostResult> distanceMatrix = HashBasedTable.create();

    public GTHierarchicalCalculator(IAntiUnifier<Tree> antiUnifier) {
        this.antiUnifier = new GTPlainAntiUnifier(antiUnifier);
        this.costCalculator = new GTCostCalculator();
    }

    public List<Cluster<GTTreeEdit>> cluster(List<GTTreeEdit> edits) {
        List<Cluster<GTTreeEdit>> clusters = new ArrayList<>();
        this.cluster(edits, clusters::add);
        return clusters;
    }

    public void cluster(List<GTTreeEdit> edits, Consumer<Cluster<GTTreeEdit>> onClustered) {
        if (edits.isEmpty()) {
            return;
        }
        List<Cluster<GTTreeEdit>> workList = new LinkedList<>(edits.stream().map(edit -> new Cluster<>(edit, Collections.emptyList())).toList());
        Stack<Cluster<GTTreeEdit>> stack = new Stack<>();

        int total = edits.size(), finish = 1;
        while (workList.size() > 1) {
            if (stack.isEmpty()) {
                int randomCluster = (int) (Math.random() * workList.size());
                stack.push(workList.get(randomCluster));
                continue;
            }

            Cluster<GTTreeEdit> peek = stack.peek();

            GTTreeEdit result = null;
            double minDist = Double.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < workList.size(); i++) {
                Cluster<GTTreeEdit> cluster = workList.get(i);
                if (peek == cluster) continue;

                CostResult cost = distanceMatrix.get(peek, cluster);
                if (cost == null) {
//                    if (peek.pattern().mappings() == null) peek.pattern().mappings(matcher.match(peek.pattern().before(), peek.pattern().after()));
//                    if (cluster.pattern().mappings() == null) cluster.pattern().mappings(matcher.match(cluster.pattern().before(), cluster.pattern().after()));
                    cost = costCalculator.cost(antiUnifier.antiUnify(peek.pattern(), cluster.pattern()));
//                    peek.pattern().mappings(null);
//                    cluster.pattern().mappings(null);
                    distanceMatrix.put(peek, cluster, cost);
                    distanceMatrix.put(cluster, peek, cost);
                }
                if (cost.dist() < minDist) {
                    result = cost.pattern();
                    minDist = cost.dist();
                    minIndex = i;
                }
            }

            Cluster<GTTreeEdit> target = workList.get(minIndex);
            if (stack.size() > 1 && stack.get(stack.size() - 2) == target) {
                var cluster1 = stack.pop();
                var cluster2 = stack.pop();
                workList.remove(cluster1);
                workList.remove(cluster2);
                distanceMatrix.row(cluster1).clear();
                distanceMatrix.row(cluster2).clear();
                distanceMatrix.column(cluster1).clear();
                distanceMatrix.column(cluster2).clear();
//                cluster1.pattern().mappings(null);
//                cluster2.pattern().mappings(null);
//                cluster1.pattern().modified(null);
//                cluster2.pattern().modified(null);

                if (!testResult(result)) {
                    // Discard
                    if (cluster1.children().size() == 0 && cluster2.children().size() == 0) {
                        finish += 2;
                    } else {
                        if (cluster1.children().size() > 0) {
                            onClustered.accept(cluster1);
                            finish++;
                        } else {
                            workList.add(cluster1);
                        }
                        if (cluster2.children().size() > 0) {
                            onClustered.accept(cluster2);
                            finish++;
                        } else {
                            workList.add(cluster2);
                        }
                    }
                    System.out.printf("[%d/%d] Cluster edit, discard\n", finish, total);
                    continue;
                }

                if (result.before().getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL) && Objects.equals(result.before().getType(), PlainAntiUnifier2.HOLE_TYPE)) {
                    onClustered.accept(new Cluster<>(result, List.of(cluster1, cluster2)));
                    finish += 2;
                    System.out.printf("[%d/%d] Cluster edit, cluster\n", finish, total);
                } else {
                    workList.add(new Cluster<>(result, List.of(cluster1, cluster2)));
                    System.out.printf("[%d/%d] Cluster edit, next\n", ++finish, total);
                }
            } else {
                stack.push(target);
            }
        }
        workList.forEach(onClustered);
    }

    public boolean testResult(GTTreeEdit result) {
        List<Tree> holesBefore = result.before().getDescendants();
        List<Tree> holesAfter = result.after().getDescendants();
        holesBefore.add(result.before());
        holesAfter.add(result.after());
        holesBefore.removeIf(tree -> !tree.getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL));
        holesAfter.removeIf(tree -> !tree.getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL));
        return holesAfter.stream().allMatch(after -> holesBefore.stream().anyMatch(before -> Objects.equals(before.getLabel(), after.getLabel())));
    }
}
