package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.GTAntiUnifier;
import com.cpunisher.hasakafix.antiunification.IAntiUnifier;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.tree.AUCompiler;
import com.cpunisher.hasakafix.tree.AUTree;
import com.cpunisher.hasakafix.utils.IdentityPair;

import java.util.*;

public class GTHierarchicalCalculator implements IClusterCalculator<GTTreeEdit> {
    private final IAntiUnifier antiUnifier = new GTAntiUnifier();
    public List<Cluster<GTTreeEdit>> cluster(List<GTTreeEdit> edits) {
        if (edits.isEmpty()) {
            return List.of();
        }
        List<GTTreeEdit> workList = new LinkedList<>(edits);
        List<Cluster<GTTreeEdit>> clusters = new ArrayList<>();

        GTTreeEdit firstEdit = workList.remove(0);
        Cluster<GTTreeEdit> last = new Cluster<>(GTAntiUnifier.treeToString(firstEdit.before()), GTAntiUnifier.treeToString(firstEdit.after()), List.of(firstEdit));
        clusters.add(last);
        while (!workList.isEmpty()) {
            var target = workList.stream()
                    .map(edit -> cost(last, edit)).min(Comparator.comparingDouble(before -> (before.first.cost + before.second.cost)))
                    .get();
            List<GTTreeEdit> newEdits = new LinkedList<>(last.getEdits());
            newEdits.add(target.first.edit);
            clusters.add(new Cluster<>(target.first.template, target.second.template, newEdits));
        }
        return clusters;
    }

    public IdentityPair<CostResult> cost(Cluster<GTTreeEdit> cluster, GTTreeEdit edit) {
        // TODO get min
        AntiUnifyData before = antiUnifier.antiUnify(cluster.getBeforeTemplate(), GTAntiUnifier.treeToString(edit.before())).get(0);
        AntiUnifyData after = antiUnifier.antiUnify(cluster.getAfterTemplate(), GTAntiUnifier.treeToString(edit.after())).get(0);
        return new IdentityPair<>(
                new CostResult(edit, before.template(), metrics(before)),
                new CostResult(edit, after.template(), metrics(after))
        );
    }

    private double metrics(AntiUnifyData antiUnifyData) {
        AUTree tree = AUCompiler.compile(antiUnifyData.template());
        int size = tree.size();
        int placeholder = antiUnifyData.substitutions().size();
        int substitutionCost = 0;
        for (AntiUnifySubstitution substitution : antiUnifyData.substitutions()) {
            AUTree leftTree = AUCompiler.compile(substitution.left());
            AUTree rightTree = AUCompiler.compile(substitution.right());
            substitutionCost += leftTree.size() + rightTree.size();
        }
        return (double) (substitutionCost - placeholder) / size;
    }

    private record CostResult(GTTreeEdit edit, String template, double cost) {
    }
}
