package edu.berkeley.path.lprm.lp.solver;

import edu.berkeley.path.lprm.lp.problem.*;
import edu.berkeley.path.lprm.rm.ProblemRampMetering;
import gurobi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GurobiSolver implements Solver {

    private HashMap<String,GRBVar> variables = new HashMap<String, GRBVar>();
    private static HashMap<Relation,Character> relation_map = new HashMap<Relation,Character>();
    private static HashMap<OptType,Integer> opt_map = new HashMap<OptType,Integer>();

    // gurobi environment and model
    private GRBEnv env;
    private GRBModel model;

    static {
        relation_map.put(Relation.EQ,GRB.EQUAL);
        relation_map.put(Relation.LEQ,GRB.LESS_EQUAL);
        relation_map.put(Relation.GEQ, GRB.GREATER_EQUAL);
        opt_map.put(OptType.MAX,GRB.MAXIMIZE);
        opt_map.put(OptType.MIN,GRB.MINIMIZE);
    }

    public GurobiSolver(){
        try {
            env = new GRBEnv("out\\gurobi.log");
            env.set(GRB.IntParam.OutputFlag,0);    // suppress output
            model = new GRBModel(env);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void initialize(Problem P){

        try {

            GRBLinExpr cost = new GRBLinExpr();

            // Create variables, bounds, and cost
            for(String name : P.get_unique_unknowns() ){
                Double lb = P.get_lower_bound_for(name);
                if(lb==null)
                    lb = 0d;
                Double ub = P.get_upper_bound_for(name);
                if(ub==null)
                    ub = GRB.INFINITY;
                GRBVar x = model.addVar(lb,ub,0d,GRB.CONTINUOUS, name);
                cost.addTerm(P.get_cost().get_coefficient(name),x);
                variables.put(name,x);
            }

            // Integrate new variables
            model.update();

            // Set objective
            model.setObjective(cost,opt_map.get(P.get_opt_type()));

            // Add constraints
            for(Map.Entry<String,Constraint> e: P.get_constraints().entrySet()){
                String cnst_name = e.getKey();
                Constraint cnst = e.getValue();
                GRBLinExpr expr = new GRBLinExpr();
                for(Map.Entry<String,Double> p : cnst.get_coefficients().entrySet())
                    expr.addTerm(p.getValue(),variables.get(p.getKey()));
                model.addConstr(expr,relation_map.get(cnst.get_relation()),cnst.get_rhs(),cnst_name);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void set_constraint_rhs(Problem P){
        try {
            for(Map.Entry<String,Constraint> e: P.get_constraints().entrySet()){
                String cnst_name = e.getKey();
                Constraint cnst = e.getValue();
                GRBConstr gbi_cnst = model.getConstrByName(cnst_name);
                gbi_cnst.set(GRB.DoubleAttr.RHS,cnst.get_rhs());
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PointValue solve() {
        try {

            // Optimize model
            model.optimize();

            // read result
            ArrayList<String> names = new ArrayList<String>();
            double [] values = new double [variables.size()];
            int i=0;
            for(GRBVar v : variables.values()){
                names.add(v.get(GRB.StringAttr.VarName));
                values[i++] = v.get(GRB.DoubleAttr.X);
            }

            // cast as PointValue
            PointValue pv_sol = new PointValue(names,values,model.get(GRB.DoubleAttr.ObjVal));

            return pv_sol;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    @Override
    public void discard(){
        try {
            model.dispose();
            env.dispose();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}
