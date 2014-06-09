package lp.solver;

import lp.problem.OptType;
import lp.problem.PointValue;
import lp.problem.Problem;
import lp.problem.Relation;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.linear.Relationship;
import org.apache.commons.math3.optimization.linear.SimplexSolver;

import java.util.HashMap;

/**
 * Created by gomes on 6/4/14.
 */
public class ApacheSolver implements Solver {

    public static HashMap<Relation,Relationship> relation_map = new HashMap<Relation,Relationship>();
    public static HashMap<OptType,GoalType> opt_map = new HashMap<OptType,GoalType>();
    static {
        relation_map.put(Relation.EQ  , Relationship.EQ);
        relation_map.put(Relation.LEQ , Relationship.LEQ);
        relation_map.put(Relation.GEQ , Relationship.GEQ);
        opt_map.put(OptType.MAX,GoalType.MAXIMIZE);
        opt_map.put(OptType.MIN,GoalType.MINIMIZE);
    }

    @Override
    public PointValue solve(Problem P) {
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
