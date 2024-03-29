package at.jku.risc.stout.urau;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import at.jku.risc.stout.urau.algo.AntiUnify;
import at.jku.risc.stout.urau.algo.AntiUnifyProblem;
import at.jku.risc.stout.urau.algo.AntiUnifySystem;
import at.jku.risc.stout.urau.algo.DebugLevel;
import at.jku.risc.stout.urau.algo.IllegalAlignmentException;
import at.jku.risc.stout.urau.algo.RigidityFnc;
import at.jku.risc.stout.urau.algo.RigidityFncSubsequence;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;
import at.jku.risc.stout.urau.data.MalformedTermException;
import at.jku.risc.stout.urau.data.atom.Variable;

public class TestPaperExample {

	public static void main(String[] args) throws MalformedTermException,
			IOException, IllegalAlignmentException {
		Reader in1 = new StringReader("f(g(a,a), a, X, b)"); // Use FileReader instead
		Reader in2 = new StringReader("f(g(b,b), g(Y), b)"); // Use FileReader instead
		boolean iterateAll = true;

		RigidityFnc rFnc = new RigidityFncSubsequence().setMinLen(1);
		EquationSystem<AntiUnifyProblem> eqSys = new EquationSystem<AntiUnifyProblem>() {
			public AntiUnifyProblem newEquation() {
				return new AntiUnifyProblem();
			}
		};
		new InputParser<>(eqSys).parseHedgeEquation(in1, in2);

		new AntiUnify(rFnc, eqSys, DebugLevel.SILENT) {
			public void callback(AntiUnifySystem res, Variable var) {
				System.out.println(res.getSigma().get(var));
				System.out.println(res.getStore().get(0));
			};
		}.antiUnify(iterateAll, null);
	}

}
