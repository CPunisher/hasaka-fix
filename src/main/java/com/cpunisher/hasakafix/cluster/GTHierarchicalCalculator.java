package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.GTPlainAntiUnifier;
import com.cpunisher.hasakafix.antiunification.IAntiUnifier;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeVisitor;

import java.util.*;

public class GTHierarchicalCalculator implements IClusterCalculator<GTTreeEdit> {
    private final GTPlainAntiUnifier antiUnifier;

    public GTHierarchicalCalculator(IAntiUnifier<Tree> antiUnifier) {
        this.antiUnifier = new GTPlainAntiUnifier(antiUnifier);
    }

    public Cluster<GTTreeEdit> cluster(List<GTTreeEdit> edits) {
        if (edits.isEmpty()) {
            return null;
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
                CostResult cost = cost(peek, cluster);
                if (cost.dist < minDist) {
                    result = cost.pattern;
                    minDist = cost.dist;
                    minIndex = i;
                }
            }

            Cluster<GTTreeEdit> target = workList.get(minIndex);
            if (stack.size() > 1 && stack.get(stack.size() - 2) == target) {
                var cluster1 = stack.pop();
                var cluster2 = stack.pop();
                workList.remove(cluster1);
                workList.remove(cluster2);
                workList.add(new Cluster<>(result, List.of(cluster1, cluster2)));
                System.out.printf("[%d/%d] Cluster edit\n", ++finish, total);
            } else {
                stack.push(target);
            }
        }
        return workList.get(0);
    }

    public CostResult cost(Cluster<GTTreeEdit> cluster1, Cluster<GTTreeEdit> cluster2) {
        GTPlainAntiUnifier.GTAntiUnifierData result = antiUnifier.antiUnify(cluster1.pattern(), cluster2.pattern());
        AntiUnifyData<Tree> beforeResult = new AntiUnifyData<>(result.template().before(), result.beforeSubs());
        AntiUnifyData<Tree> afterResult = new AntiUnifyData<>(result.template().after(), result.afterSubs());
        return new CostResult(result.template(), metrics(beforeResult) + metrics(afterResult));
    }

    private double metrics(AntiUnifyData<Tree> result) {
        AntiUnificationMetrics metrics = getAUMetrics(result.template());
        int placeholder = result.substitutions().size();
        int substitutionCost = 0;
        for (AntiUnifySubstitution<Tree> substitution : result.substitutions()) {
            AntiUnificationMetrics leftMetrics = getAUMetrics(substitution.left());
            AntiUnificationMetrics rightMetrics = getAUMetrics(substitution.right());
            substitutionCost += leftMetrics.leftSize + rightMetrics.leftSize;
        }
        return (double) (substitutionCost - placeholder) / metrics.size;
    }

    private static final String KEY_AU_METRICS = "AUMetrics";
    private static AntiUnificationMetrics getAUMetrics(Tree tree) {
        AntiUnificationMetrics metrics = (AntiUnificationMetrics) tree.getMetadata(KEY_AU_METRICS);
        if (metrics == null) {
            TreeVisitor.visitTree(tree, new AntiUnificationMetricsComputer());
            metrics = (AntiUnificationMetrics) tree.getMetadata(KEY_AU_METRICS);
        }
        return metrics;
    }

    public record CostResult(GTTreeEdit pattern, double dist) {}

    private record AntiUnificationMetrics(int size, int leftSize) {}

    private static class AntiUnificationMetricsComputer extends TreeVisitor.InnerNodesAndLeavesVisitor {
        @Override
        public void visitLeaf(Tree tree) {
            tree.setMetadata(KEY_AU_METRICS, new AntiUnificationMetrics(1, 1));
        }

        @Override
        public void endInnerNode(Tree tree) {
            int sumSize = 0;
            int sumLeftSize = 0;
            for (Tree child : tree.getChildren()) {
                AntiUnificationMetrics metrics = getAUMetrics(child);
                sumSize += metrics.size;
                sumLeftSize += metrics.leftSize;
            }
            tree.setMetadata(KEY_AU_METRICS, new AntiUnificationMetrics(sumSize + 1, sumLeftSize + 1));
        }
    }
}
