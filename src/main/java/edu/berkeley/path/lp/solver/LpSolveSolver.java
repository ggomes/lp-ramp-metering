package edu.berkeley.path.lp.solver;

import edu.berkeley.path.lp.problem.*;
//import lpsolve.LpSolve;
//import lpsolve.LpSolveException;

import java.util.*;

/**
* Created by gomes on 6/9/14.
*/
public class LpSolveSolver { //implements Solver {

//    public static HashMap<Relation,Integer> relation_map = new HashMap<Relation,Integer>();
//    static {
//        relation_map.put(Relation.EQ,LpSolve.EQ);
//        relation_map.put(Relation.LEQ,LpSolve.LE);
//        relation_map.put(Relation.GEQ,LpSolve.GE);
//    }
//
//    @Override
//    public PointValue solve(Problem P){
//
//        double [] opt_values = null;
//        double opt_cost = Double.NaN;
//        ArrayList<String> unknowns = null;
//
//        try {
//
//            unknowns = P.get_unique_unknowns();
//            int num_unknowns = unknowns.size();
//
//            // Create a problem
//            LpSolve solver = LpSolve.makeLp(0,num_unknowns);
//
//            if(solver.getLp()==0)
//                System.err.println("Couldn't construct a new model.");
//
//            // name the variables
//            for(int i=0;i<num_unknowns;i++)
//                solver.setColName(i+1,unknowns.get(i));
//
//            // makes building the model faster if it is done rows by row
//            solver.setAddRowmode(true);
//
//            // add constraints
//            Iterator cit = P.get_constraints().entrySet().iterator();
//            while (cit.hasNext()) {
//                Map.Entry pairs = (Map.Entry)cit.next();
//                String name = (String) pairs.getKey();
//                Linear L = (Linear) pairs.getValue();
//                double value = L.get_rhs();
//                double[] coef = new double[num_unknowns+1];
//                for(int i=0;i<num_unknowns;i++)
//                    coef[i+1] = L.get_coefficient(unknowns.get(i));
//                Integer relation = LpSolveSolver.relation_map.get(L.get_relation());
//                solver.addConstraint(coef, relation, value);
//                solver.setRowName(solver.getNrows(),name);
//            }
//
//            // add upper bounds
//            Iterator ubit = P.get_upper_bounds().entrySet().iterator();
//            while (ubit.hasNext()) {
//                Map.Entry pairs = (Map.Entry)ubit.next();
//                String name = (String) pairs.getKey();
//                int ind = unknowns.indexOf(name);
//                double[] coef = new double[num_unknowns+1];
//                coef[ind+1] = 1d;
//                solver.addConstraint(coef, LpSolve.LE, (Double) pairs.getValue());
//                solver.setRowName(solver.getNrows(),"ub_"+name);
//            }
//
//            // add lower bounds
//            Iterator lbit = P.get_lower_bounds().entrySet().iterator();
//            while (lbit.hasNext()) {
//                Map.Entry pairs = (Map.Entry)lbit.next();
//                String name = (String) pairs.getKey();
//                int ind = unknowns.indexOf(name);
//                double[] coef = new double[num_unknowns+1];
//                coef[ind+1] = 1d;
//                solver.addConstraint(coef, LpSolve.GE, (Double) pairs.getValue());
//                solver.setRowName(solver.getNrows(),"lb_"+name);
//            }
//
//            // rowmode should be turned off again when done building the model
//            solver.setAddRowmode(false);
//
//            // set objective function
//            double[] coef = new double[num_unknowns+1];
//            for(int i=0;i<num_unknowns;i++)
//                coef[i+1] = P.get_cost().get_coefficient(unknowns.get(i));
//            solver.setObjFn(coef);
//
//            // set the object direction to maximize
//            switch(P.get_opt_type()){
//                case MAX:
//                    solver.setMaxim();
//                    break;
//                case MIN:
//                    solver.setMinim();
//                    break;
//            }
//
//            // print problem to file
////            System.out.println("Writing files.");
////            solver.writeLp("data\\output\\model.lp");
////            solver.writeMps("data\\output\\model.mps");
//            solver.printLp();
//
//            // I only want to see important messages on screen while solving
//            solver.setVerbose(LpSolve.IMPORTANT);
//
////            System.out.println("LpSolve unknowns: " + solver.getNcolumns());
////            System.out.println("LpSolve constraints: " + solver.getNrows());
//
//            // solve the problem
//            solver.solve();
//
//            // print solution
//            opt_values = solver.getPtrVariables();
//            opt_cost = solver.getObjective();
//
//            // delete the problem and free memory
//            if(solver.getLp()!=0)
//                solver.deleteLp();
//        }
//        catch (LpSolveException e) {
//            e.printStackTrace();
//        }
//
//        return new PointValue(unknowns,opt_values,opt_cost);
//
//    }

}