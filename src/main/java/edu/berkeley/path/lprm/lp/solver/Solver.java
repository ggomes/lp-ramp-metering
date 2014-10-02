package edu.berkeley.path.lprm.lp.solver;

import edu.berkeley.path.lprm.lp.problem.PointValue;
import edu.berkeley.path.lprm.lp.problem.Problem;

/**
 * Created by gomes on 6/9/2014.
 */
public interface Solver {
    public PointValue solve(Problem P);
}
