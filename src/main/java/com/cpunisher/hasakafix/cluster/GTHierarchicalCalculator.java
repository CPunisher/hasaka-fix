package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.GTPlainAntiUnifier;
import com.cpunisher.hasakafix.antiunification.IAntiUnifier;
import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.tree.Tree;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;

public class GTHierarchicalCalculator implements IClusterCalculator<GTTreeEdit> {
    private final GTPlainAntiUnifier antiUnifier;
    private final GTCostCalculator costCalculator;
    private final Table<Cluster<GTTreeEdit>, Cluster<GTTreeEdit>, CostResult> distanceMatrix = HashBasedTable.create();

    public GTHierarchicalCalculator(IAntiUnifier<Tree> antiUnifier) {
        this.antiUnifier = new GTPlainAntiUnifier(antiUnifier);
        this.costCalculator = new GTCostCalculator();
    }

    public List<Cluster<GTTreeEdit>> cluster(List<GTTreeEdit> edits) {
        if (edits.isEmpty()) {
            return null;
        }
        List<Cluster<GTTreeEdit>> clusters = new LinkedList<>();
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
                    cost = costCalculator.cost(antiUnifier.antiUnify(peek.pattern(), cluster.pattern()));
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
                if (result.before().getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL) && Objects.equals(result.before().getType(), PlainAntiUnifier2.HOLE_TYPE)) {
                    clusters.add(new Cluster<>(result, List.of(cluster1, cluster2)));
                    finish += 2;
                    System.out.printf("[%d/%d] Cluster edit\n", finish, total);
                } else {
                    workList.add(new Cluster<>(result, List.of(cluster1, cluster2)));
                    System.out.printf("[%d/%d] Cluster edit\n", ++finish, total);
                }
            } else {
                stack.push(target);
            }
        }
        clusters.addAll(workList);
        return clusters;
    }

}
