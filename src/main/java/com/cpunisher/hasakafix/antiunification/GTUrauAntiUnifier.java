package com.cpunisher.hasakafix.antiunification;

import at.jku.risc.stout.urau.algo.*;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.github.gumtreediff.tree.Tree;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.StringJoiner;

public class GTUrauAntiUnifier implements IAntiUnifier<String> {
    @Override
    public List<AntiUnifyData<String>> antiUnify(String left, String right) {
        return unify(left, right);
    }

    // TODO abstract string value, array dimenson ...
    // https://github.com/reudismam/Revisar/blob/9d31ddcfa4f6605ab781ff9bba2b90b41743df05/src/main/java/br/ufcg/spg/equation/EquationUtils.java#L118
    public static String treeToString(Tree tree) {
        List<Tree> children = tree.getChildren();
        if (children.isEmpty()) {
            String label = tree.getLabel();
            label = label.replace("+", "_plus")
                    .replace("-", "_minus")
                    .replace("*", "_multiply")
                    .replace("/", "_divide")
                    .replace("%", "_mod")
                    .replace("==", "_equals");
            if (label.length() > 0) label = "_" + label;
            return tree.getType() + "(" + label + ")";
        }
        StringJoiner joiner = new StringJoiner(", ");
        children.forEach(child -> joiner.add(treeToString(child)));
        return tree.getType() + "(" + joiner + ")";
    }

    // anti-unification algorithm
    private List<AntiUnifyData<String>> unify(String left, String right) {
        Reader in1 = new StringReader(left);
        Reader in2 = new StringReader(right);
        boolean iterateAll = true;

        RigidityFnc func = new RigidityFncSubsequence().setMinLen(1);
        EquationSystem<AntiUnifyProblem> equationSystem = new EquationSystem<AntiUnifyProblem>() {
            @Override
            public AntiUnifyProblem newEquation() {
                return new AntiUnifyProblem();
            }
        };

        try {
            new InputParser<>(equationSystem).parseHedgeEquation(in1, in2);
            AntiUnifyHook antiUnifier = new AntiUnifyHook(func, equationSystem, DebugLevel.SILENT);
            antiUnifier.antiUnify(iterateAll, System.out);
            return antiUnifier.getDataList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
