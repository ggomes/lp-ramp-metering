package lp.solver;

import gurobi.*;
import lp.problem.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GurobiSolver implements Solver {

    private HashMap<String,GRBVar> variables = new HashMap<String,GRBVar>();
    private static HashMap<Relation,Character> relation_map = new HashMap<Relation,Character>();
    private static HashMap<OptType,Integer> opt_map = new HashMap<OptType,Integer>();
    static {
        relation_map.put(Relation.EQ,GRB.EQUAL);
        relation_map.put(Relation.LEQ,GRB.LESS_EQUAL);
        relation_map.put(Relation.GEQ,GRB.GREATER_EQUAL);
        opt_map.put(OptType.MAX,GRB.MAXIMIZE);
        opt_map.put(OptType.MIN,GRB.MINIMIZE);
    }

    @Override
    public PointValue solve(Problem P) {

        PointValue result = null;

        HashMap<String,Double> upper_bounds = P.get_upper_bounds();
        HashMap<String,Double> lower_bounds = P.get_lower_bounds();

        try {

            GRBEnv    env   = new GRBEnv(); //new GRBEnv("data/out/gurobi.log");

            env.set(GRB.IntParam.OutputFlag,0);    // suppress output
            GRBModel  model = new GRBModel(env);
            GRBLinExpr cost = new GRBLinExpr();

            // Create variables, bounds, and cost
            for(String name : P.get_unique_unknowns() ){
                Double lb = lower_bounds.get(name);
                if(lb==null)
                    lb = 0d;
                Double ub = upper_bounds.get(name);
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
            Iterator it = P.get_constraints().entrySet().iterator();
            while(it.hasNext()){
                Map.Entry pairs = (Map.Entry)it.next();
                Linear L = (Linear) pairs.getValue();
                String const_name = (String) pairs.getKey();
                GRBLinExpr expr = new GRBLinExpr();
                Iterator cit = L.get_coefficients().entrySet().iterator();
                while(cit.hasNext()){
                    Map.Entry p = (Map.Entry)cit.next();
                    expr.addTerm((Double) p.getValue(),variables.get(p.getKey()));
                }
                model.addConstr(expr,relation_map.get(L.get_relation()),L.get_rhs(),const_name);
            }

            // Optimize model
            model.optimize();

            // read result
            result = new PointValue();
            result.set_cost(model.get(GRB.DoubleAttr.ObjVal));
            for(GRBVar v : variables.values())
                result.add_value(v.get(GRB.StringAttr.VarName),v.get(GRB.DoubleAttr.X));

            // Dispose of model and environment
            model.dispose();
            env.dispose();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }

        return result;
    }

}
