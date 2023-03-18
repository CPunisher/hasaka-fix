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

    public List<Cluster<GTTreeEdit>> cluster(List<GTTreeEdit> edits) {
        if (edits.isEmpty()) {
            return List.of();
        }
        List<GTTreeEdit> workList = new LinkedList<>(edits);
        List<Cluster<GTTreeEdit>> clusters = new ArrayList<>();

        GTTreeEdit firstEdit = workList.remove(0);
        Cluster<GTTreeEdit> last = new Cluster<>(firstEdit, List.of(firstEdit));
        clusters.add(last);
        int total = edits.size(), finish = 1;
        while (!workList.isEmpty()) {
            // Calculate min cost
            CostResult target = null;
            for (GTTreeEdit edit : workList) {
                CostResult result = cost(last, edit);
                if (target == null || result.dist < target.dist) {
                    target = result;
                }
            }
            workList.remove(target.merged);

            // Append to clusters
            List<GTTreeEdit> newEdits = new LinkedList<>(last.getEdits());
            newEdits.add(target.merged);
            last = new Cluster<>(target.pattern, newEdits);
            clusters.add(last);
            System.out.printf("[%d/%d] Cluster edit\n", ++finish, total);
        }
        return clusters;
    }

    public CostResult cost(Cluster<GTTreeEdit> cluster, GTTreeEdit edit) {
        // TODO get min
        GTPlainAntiUnifier.GTAntiUnifierData result = antiUnifier.antiUnify(cluster.getPattern(), edit);
        AntiUnifyData<Tree> beforeResult = new AntiUnifyData<>(result.template().before(), result.beforeSubs());
        AntiUnifyData<Tree> afterResult = new AntiUnifyData<>(result.template().after(), result.afterSubs());
        return new CostResult(result.template(), edit, metrics(beforeResult) + metrics(afterResult));
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

    private record CostResult(GTTreeEdit pattern, GTTreeEdit merged, double dist) {}

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
