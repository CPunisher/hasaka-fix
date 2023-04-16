package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.GTPlainAntiUnifier;
import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeVisitor;
import com.github.gumtreediff.tree.TypeSet;

public class GTCostCalculator {
    public CostResult cost(GTPlainAntiUnifier.GTAntiUnifierData result) {
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
        double r = (double) (substitutionCost + placeholder) / metrics.size;
        if (substitutionCost == 0 && placeholder == 0 && metrics.size == 1 && result.template().getType() == TypeSet.type("Block")) {
            r += 1000;
        }

        return r;
    }

    public static final String KEY_AU_METRICS = "AUMetrics";
    public static AntiUnificationMetrics getAUMetrics(Tree tree) {
        AntiUnificationMetrics metrics = (AntiUnificationMetrics) tree.getMetadata(KEY_AU_METRICS);
        if (metrics == null) {
            TreeVisitor.visitTree(tree, new AntiUnificationMetricsComputer());
            metrics = (AntiUnificationMetrics) tree.getMetadata(KEY_AU_METRICS);
        }
        return metrics;
    }

    public record AntiUnificationMetrics(int size, int leftSize) {}

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
            tree.setMetadata(KEY_AU_METRICS, new AntiUnificationMetrics(sumSize + 1, sumLeftSize));
        }
    }
}
