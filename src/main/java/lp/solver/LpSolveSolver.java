package lp.solver;

import lp.problem.*;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.HashMap;

/**
 * Created by gomes on 6/9/14.
 */
public class LpSolveSolver {

    public static HashMap<Relation,Integer> relation_map = new HashMap<Relation,Integer>();
//    public static HashMap<OptType,GoalType> opt_map = new HashMap<OptType,GoalType>();
    static {
        relation_map.put(Relation.EQ,LpSolve.EQ);
        relation_map.put(Relation.LEQ,LpSolve.LE);
        relation_map.put(Relation.GEQ,LpSolve.GE);
//        opt_map.put(OptType.MAX,GoalType.MAXIMIZE);
//        opt_map.put(OptType.MIN,GoalType.MINIMIZE);
    }


    public PointValue solve(Problem P){

        try {

            String [] unknowns = P.get_unique_unknowns();
            int num_unknowns = unknowns.length;
            int num_constraints = P.get_num_bounds() + P.get_num_constraints();

            // Create a problem with 4 variables and 0 constraints
            LpSolve solver = LpSolve.makeLp(num_unknowns,num_constraints);

            // add constraints
            for(Linear L : P.constraints.values()){
                double value = L.get_rhs();
                double[] coef = new double[num_unknowns];
                for(int i=0;i<num_unknowns;i++)
                    coef[i] = L.get_coefficient(unknowns[i]);
                Integer relation = LpSolveSolver.relation_map.get(L.get_relation());
                solver.addConstraint(coef, relation, value);
            }

            // add bounds
            for(Linear L : P.bounds.values()){
                double value = L.get_rhs();
                double[] coef = new double[num_unknowns];
                for(int i=0;i<num_unknowns;i++)
                    coef[i] = L.get_coefficient(unknowns[i]);
                Integer relation = LpSolveSolver.relation_map.get(L.get_relation());
                solver.addConstraint(coef, relation, value);
            }

            // set objective function
            double[] coef = new double[num_unknowns];
            for(int i=0;i<num_unknowns;i++)
                coef[i] = P.cost.get_coefficient(unknowns[i]);
            solver.setObjFn(coef);

            // solve the problem
            solver.solve();

            // print solution
            System.out.println("Value of objective function: " + solver.getObjective());
            double[] var = solver.getPtrVariables();
            for (int i = 0; i < var.length; i++) {
                System.out.println("Value of var[" + i + "] = " + var[i]);
            }

            // delete the problem and free memory
            solver.deleteLp();
        }
        catch (LpSolveException e) {
            e.printStackTrace();
        }

        return null;
    }

}
