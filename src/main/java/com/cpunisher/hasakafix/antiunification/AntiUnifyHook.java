package com.cpunisher.hasakafix.antiunification;

import at.jku.risc.stout.urau.algo.*;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.data.atom.Variable;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;

import java.util.ArrayList;
import java.util.List;

public class AntiUnifyHook extends AntiUnify {

    private List<AntiUnifyData<String>> dataList = new ArrayList<>();

    public AntiUnifyHook(RigidityFnc rigidFnc, EquationSystem<AntiUnifyProblem> eq, DebugLevel debugLevel) {
        super(rigidFnc, eq, debugLevel);
    }

    @Override
    public void callback(AntiUnifySystem result, Variable generalizationVar) {
        TermNode hedge = result.getSigma().get(generalizationVar);
        List<AntiUnifySubstitution<String>> list = result.getStore().stream().map(problem -> new AntiUnifySubstitution<>(problem.toString(), problem.getLeft().toString(), problem.getRight().toString())).toList();
        dataList.add(new AntiUnifyData<String>(hedge.toString(), list));
    }

    public List<AntiUnifyData<String>> getDataList() {
        return dataList;
    }
}
