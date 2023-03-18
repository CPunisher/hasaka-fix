package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.GTUrauAntiUnifier;
import com.cpunisher.hasakafix.antiunification.IAntiUnifier;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.tree.AUTree;
import com.cpunisher.hasakafix.utils.IdentityPair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Deprecated
public class GTClusterCalculator {
//
//    @Override
//    public List<Cluster<IdentityPair<String>>> cluster(List<IdentityPair<String>> edits) {
//        IAntiUnifier<String> antiUnifier = new GTUrauAntiUnifier();
//        List<Cluster<IdentityPair<String>>> clusters = new ArrayList<>();
//        for (var edit : edits) {
//            AntiUnifyData<String> antiUnifyData = antiUnifier.antiUnify(edit.first, edit.second).get(0);
//            List<CostResult> costs = cost(clusters, antiUnifyData);
//            var targetOpt = costs.stream().sorted(Comparator.comparing(p -> p.cost)).filter(cost -> validate(cost.cluster, edit)).findFirst();
//            if (targetOpt.isPresent()) {
//                CostResult target = targetOpt.get();
//                target.cluster.setTemplate(target.template);
//                target.cluster.getEdits().add(edit);
//            } else {
//                AntiUnifyData initialTemplate = antiUnifier.antiUnify(edit.before(), edit.after()).get(0);
//                List<GTTreeEdit> list = new ArrayList<>();
//                list.add(edit);
//                clusters.add(new Cluster<>(initialTemplate.template(), list));
//            }
//        }
//        return clusters;
//    }
//
//    private boolean validate(Cluster<IdentityPair<String>> cluster, IdentityPair<String> edit) {
//        return true;
//    }
//
//    public List<CostResult> cost(List<Cluster<IdentityPair<String>>> clusters, AntiUnifyData<String> antiUnifyData) {
//        IAntiUnifier<String> unifier = new GTUrauAntiUnifier();
//        List<CostResult> costs = new ArrayList<>();
//        for (Cluster<IdentityPair<String>> cluster : clusters) {
//            // TODO get min
//            AntiUnifyData result = unifier.antiUnify(cluster.getTemplate(), antiUnifyData.template()).get(0);
//            AUTree tree =  AUCompiler.compile(result.template());
//            int size = tree.size();
//            int placeholder = result.substitutions().size();
//            int substitutionCost = 0;
//            for (AntiUnifySubstitution substitution : result.substitutions()) {
//                AUTree leftTree = AUCompiler.compile(substitution.left());
//                AUTree rightTree = AUCompiler.compile(substitution.right());
//                substitutionCost += leftTree.size() + rightTree.size();
//            }
//            costs.add(new CostResult(cluster, result.template(), (double) (substitutionCost - placeholder) / size));
//        }
//        return costs;
//    }
//
//    private record CostResult(Cluster<IdentityPair<String>> cluster, String template, double cost) {}
}
