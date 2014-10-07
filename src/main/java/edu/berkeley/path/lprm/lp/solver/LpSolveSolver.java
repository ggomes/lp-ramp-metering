package edu.berkeley.path.lprm.lp.solver;

import edu.berkeley.path.lprm.lp.problem.*;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.*;

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
    private LpSolve lp_solver;

    @Override
    public PointValue solve(Problem P){

        double [] opt_values = null;
        double opt_cost = Double.NaN;
        ArrayList<String> unknowns = null;

        try {

            unknowns = P.get_unique_unknowns();
            int num_unknowns = unknowns.size();

            // Create a problem
            lp_solver = LpSolve.makeLp(0,num_unknowns);

            if(lp_solver.getLp()==0)
                System.err.println("Couldn't construct a new model.");

            // name the variables
            for(int i=0;i<num_unknowns;i++)
                lp_solver.setColName(i+1,unknowns.get(i));

            // makes building the model faster if it is done rows by row
            lp_solver.setAddRowmode(true);

            // add constraints
            Iterator cit = P.get_constraints().entrySet().iterator();
            while (cit.hasNext()) {
                Map.Entry pairs = (Map.Entry)cit.next();
                String name = (String) pairs.getKey();
                Linear L = (Linear) pairs.getValue();
                double value = L.get_rhs();
                double[] coef = new double[num_unknowns+1];
                for(int i=0;i<num_unknowns;i++)
                    coef[i+1] = L.get_coefficient(unknowns.get(i));
                Integer relation = LpSolveSolver.relation_map.get(L.get_relation());
                lp_solver.addConstraint(coef, relation, value);
                lp_solver.setRowName(lp_solver.getNrows(),name);
            }

            // add upper bounds
            Iterator ubit = P.get_upper_bounds().entrySet().iterator();
            while (ubit.hasNext()) {
                Map.Entry pairs = (Map.Entry)ubit.next();
                String name = (String) pairs.getKey();
                int ind = unknowns.indexOf(name);
                double[] coef = new double[num_unknowns+1];
                coef[ind+1] = 1d;
                lp_solver.addConstraint(coef, LpSolve.LE, (Double) pairs.getValue());
                lp_solver.setRowName(lp_solver.getNrows(),"ub_"+name);
            }

            // add lower bounds
            Iterator lbit = P.get_lower_bounds().entrySet().iterator();
            while (lbit.hasNext()) {
                Map.Entry pairs = (Map.Entry)lbit.next();
                String name = (String) pairs.getKey();
                int ind = unknowns.indexOf(name);
                double[] coef = new double[num_unknowns+1];
                coef[ind+1] = 1d;
                lp_solver.addConstraint(coef, LpSolve.GE, (Double) pairs.getValue());
                lp_solver.setRowName(lp_solver.getNrows(),"lb_"+name);
            }

            // rowmode should be turned off again when done building the model
            lp_solver.setAddRowmode(false);

            // set objective function
            double[] coef = new double[num_unknowns+1];
            for(int i=0;i<num_unknowns;i++)
                coef[i+1] = P.get_cost().get_coefficient(unknowns.get(i));
            lp_solver.setObjFn(coef);

            // set the object direction to maximize
            switch(P.get_opt_type()){
                case MAX:
                    lp_solver.setMaxim();
                    break;
                case MIN:
                    lp_solver.setMinim();
                    break;
            }

            // print problem to file
//            System.out.println("Writing files.");
//            lp_solver.writeLp("data\\output\\model.lp");
//            lp_solver.writeMps("data\\output\\model.mps");

            // I only want to see important messages on screen while solving
            lp_solver.setVerbose(LpSolve.IMPORTANT);

//            System.out.println("LpSolve unknowns: " + lp_solver.getNcolumns());
//            System.out.println("LpSolve constraints: " + lp_solver.getNrows());

            // solve the problem
            lp_solver.solve();

            // print solution
            opt_values = lp_solver.getPtrVariables();
            opt_cost = lp_solver.getObjective();

            // delete the problem and free memory
            if(lp_solver.getLp()!=0)
                lp_solver.deleteLp();
        }
        catch (LpSolveException e) {
            e.printStackTrace();
        }

        return new PointValue(unknowns,opt_values,opt_cost);

    }


    @Override
    public String toString() {
        return super.toString();
    }
}