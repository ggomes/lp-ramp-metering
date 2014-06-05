package lp.solver;

import lp.problem.PointValue;
import lp.problem.Problem;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.linear.SimplexSolver;

/**
 * Created by gomes on 6/4/14.
 */
public class ApacheSolver {

    public PointValue solve(Problem P){
        ApacheProblem aP = new ApacheProblem(P);
        SimplexSolver solver = new SimplexSolver();
        PointValuePair pair = solver.optimize(
                aP.get_obj(),
                aP.get_constraints(),
                aP.get_goalType(),
                false );
        return new PointValue(aP.get_unknowns(),pair.getPoint(),pair.getValue());
    }

}
