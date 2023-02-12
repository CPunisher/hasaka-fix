package com.cpunisher.hasakafix.antiunification;

import at.jku.risc.stout.urau.algo.*;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.data.atom.Variable;
import com.cpunisher.hasakafix.antiunification.bean.AUData;

import java.util.ArrayList;

public class AntiUnifyHole extends AntiUnify {

    private AUData data;

    public AntiUnifyHole(RigidityFnc rigidFnc, EquationSystem<AntiUnifyProblem> eq, DebugLevel debugLevel) {
        super(rigidFnc, eq, debugLevel);
    }

    @Override
    public void callback(AntiUnifySystem result, Variable generalizationVar) {
        TermNode hedge = result.getSigma().get(generalizationVar);
        this.data = new AUData(hedge.toString(), new ArrayList<>());
    }

    public AUData getData() {
        return data;
    }
}
