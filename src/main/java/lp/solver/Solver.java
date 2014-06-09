package lp.solver;

import lp.problem.PointValue;
import lp.problem.Problem;

/**
 * Created by gomes on 6/9/2014.
 */
public interface Solver {
    public PointValue solve(Problem P);
}
