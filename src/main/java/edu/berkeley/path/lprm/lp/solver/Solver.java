package edu.berkeley.path.lprm.lp.solver;

import edu.berkeley.path.lprm.lp.problem.PointValue;
import edu.berkeley.path.lprm.lp.problem.Problem;

/**
 * Created by gomes on 6/9/2014.
 */
public interface Solver {
    public PointValue solve();
    public void initialize(Problem P);
    public void set_constraint_rhs(Problem P);
    public void discard();
}
