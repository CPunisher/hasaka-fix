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

public class GTHierarchicalCalculator implements IClusterCalculator<IdentityPair<String>> {
    private final IAntiUnifier antiUnifier = new GTAntiUnifier();
    public List<Cluster<IdentityPair<String>>> cluster(List<IdentityPair<String>> edits) {
        if (edits.isEmpty()) {
            return List.of();
        }
        List<IdentityPair<String>> workList = new LinkedList<>(edits);
        List<Cluster<IdentityPair<String>>> clusters = new ArrayList<>();

        IdentityPair<String> firstEdit = workList.remove(0);
        Cluster<IdentityPair<String>> last = new Cluster<>(firstEdit.first, firstEdit.second, List.of(firstEdit));
        clusters.add(last);
        while (!workList.isEmpty()) {
            var target = workList.stream()
                    .map(edit -> cost(last, edit)).min(Comparator.comparingDouble(before -> (before.first.cost + before.second.cost)))
                    .get();
            List<IdentityPair<String>> newEdits = new LinkedList<>(last.getEdits());
            newEdits.add(target.first.edit);
            clusters.add(new Cluster<>(target.first.template, target.second.template, newEdits));
        }
        return clusters;
    }

    public IdentityPair<CostResult> cost(Cluster<?> cluster, IdentityPair<String> edit) {
        // TODO get min
        AntiUnifyData before = antiUnifier.antiUnify(cluster.getBeforeTemplate(), edit.first).get(0);
        AntiUnifyData after = antiUnifier.antiUnify(cluster.getAfterTemplate(), edit.second).get(0);
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

    private record CostResult(IdentityPair<String> edit, String template, double cost) {
    }
}
