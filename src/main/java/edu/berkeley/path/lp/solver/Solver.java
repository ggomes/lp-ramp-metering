package edu.berkeley.path.lp.solver;

import edu.berkeley.path.lp.problem.PointValue;
import edu.berkeley.path.lp.problem.Problem;

/**
 * Created by gomes on 6/9/2014.
 */
public interface Solver {
    public PointValue solve(Problem P);
}
