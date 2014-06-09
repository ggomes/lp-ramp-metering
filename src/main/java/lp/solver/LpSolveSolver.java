package lp.solver;

import lp.problem.*;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gomes on 6/9/14.
 */
public class LpSolveSolver implements Solver {

    public static HashMap<Relation,Integer> relation_map = new HashMap<Relation,Integer>();
    static {
        relation_map.put(Relation.EQ,LpSolve.EQ);
        relation_map.put(Relation.LEQ,LpSolve.LE);
        relation_map.put(Relation.GEQ,LpSolve.GE);
    }


    @Override
    public PointValue solve(Problem P){

        double [] opt_values = null;
        double opt_cost = Double.NaN;
        String [] unknowns = null;

        try {

            unknowns = P.get_unique_unknowns();
            int num_unknowns = unknowns.length;
            int num_constraints = P.get_num_bounds() + P.get_num_constraints();

            // Create a problem
            LpSolve solver = LpSolve.makeLp(0,num_unknowns);

            if(solver.getLp()==0)
                System.err.println("Couldn't construct a new model.");

            // name the variables
            for(int i=0;i<num_unknowns;i++)
                solver.setColName(i+1,unknowns[i]);

            // makes building the model faster if it is done rows by row
            solver.setAddRowmode(true);

            // add constraints

            Iterator cit = P.constraints.entrySet().iterator();
            while (cit.hasNext()) {
                Map.Entry pairs = (Map.Entry)cit.next();
                String name = (String) pairs.getKey();
                Linear L = (Linear) pairs.getValue();
                double value = L.get_rhs();
                double[] coef = new double[num_unknowns];
                for(int i=0;i<num_unknowns;i++)
                    coef[i] = L.get_coefficient(unknowns[i]);
                Integer relation = LpSolveSolver.relation_map.get(L.get_relation());
                solver.addConstraint(coef, relation, value);
                solver.setRowName(solver.getNrows(),name);
            }

            // add bounds
            Iterator bit = P.bounds.entrySet().iterator();
            while (bit.hasNext()) {
                Map.Entry pairs = (Map.Entry)bit.next();
                String name = (String) pairs.getKey();
                Linear L = (Linear) pairs.getValue();
                double value = L.get_rhs();
                double[] coef = new double[num_unknowns];
                for(int i=0;i<num_unknowns;i++)
                    coef[i] = L.get_coefficient(unknowns[i]);
                Integer relation = LpSolveSolver.relation_map.get(L.get_relation());
                solver.addConstraint(coef, relation, value);
                solver.setRowName(solver.getNrows(),name);
            }

            // rowmode should be turned off again when done building the model
            solver.setAddRowmode(false);

            // set objective function
            double[] coef = new double[num_unknowns];
            for(int i=0;i<num_unknowns;i++)
                coef[i] = P.cost.get_coefficient(unknowns[i]);
            solver.setObjFn(coef);

            // set the object direction to maximize
            switch(P.opt_type){
                case MAX:
                    solver.setMaxim();
                    break;
                case MIN:
                    solver.setMinim();
            }

            // print problem to file
            solver.writeLp("data\\output\\model.lp");
            solver.writeMps("data\\output\\model.mps");

            // I only want to see important messages on screen while solving
            solver.setVerbose(LpSolve.IMPORTANT);

            // solve the problem
            int ret = solver.solve();

            if(ret == LpSolve.OPTIMAL)
                ret = 0;
            else
                ret = 5;

            // print solution
            System.out.println("Value of objective function: " + solver.getObjective());
            opt_values = solver.getPtrVariables();
            opt_cost = solver.getObjective();
            for (int i = 0; i < opt_values.length; i++)
                System.out.println(solver.getColName(i+1) + " = " + opt_values[i]);

            // delete the problem and free memory
            if(solver.getLp()!=0)
                solver.deleteLp();
        }
        catch (LpSolveException e) {
            e.printStackTrace();
        }

        return new PointValue(unknowns,opt_values,opt_cost);

    }

}
