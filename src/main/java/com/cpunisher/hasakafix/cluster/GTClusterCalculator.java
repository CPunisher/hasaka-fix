package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.GTAntiUnifier;
import com.cpunisher.hasakafix.antiunification.IAntiUnifier;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.tree.AUCompiler;
import com.cpunisher.hasakafix.tree.AUTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class GTClusterCalculator implements IClusterCalculator<GTTreeEdit> {
    public List<Pair<String, Double>> cost(List<Cluster<GTTreeEdit>> clusters, AntiUnifyData antiUnifyData) {
        IAntiUnifier<Tree> unifier = new GTAntiUnifier();
        List<Pair<String, Double>> costs = new ArrayList<>();
        for (Cluster<GTTreeEdit> cluster : clusters) {
            // TODO get min
            AntiUnifyData result = unifier.antiUnify(cluster.template(), antiUnifyData.template()).get(0);
            AUTree tree =  AUCompiler.compile(result.template());
            int size = tree.size();
            int placeholder = result.substitutions().size();
            int substitutionCost = 0;
            for (AntiUnifySubstitution substitution : result.substitutions()) {
                AUTree leftTree = AUCompiler.compile(substitution.left());
                AUTree rightTree = AUCompiler.compile(substitution.right());
                substitutionCost += leftTree.size() + rightTree.size();
            }
            costs.add(new Pair<>(result.template(), (double) (substitutionCost - placeholder) / size));
        }
        return costs;
    }
}
