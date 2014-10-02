package edu.berkeley.path.lp.solver;

import edu.berkeley.path.lp.problem.*;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.linear.LinearConstraint;
import org.apache.commons.math3.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optimization.linear.Relationship;
import org.apache.commons.math3.optimization.linear.SimplexSolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    private class ApacheProblem {

        private ArrayList<String>  unknowns;
        private int num_unknowns;
        private LinearObjectiveFunction obj;
        private ArrayList<LinearConstraint> constraints;
        private GoalType goalType;

        public ApacheProblem(Problem P){

            unknowns = P.get_unique_unknowns();
            num_unknowns = unknowns.size();

            // cost function
            double[] coefficients = new double[num_unknowns];
            for(int i=0;i<num_unknowns;i++)
                coefficients[i] = P.get_cost().get_coefficient(unknowns.get(i));
            this.obj = new LinearObjectiveFunction(coefficients,0d);

            // constraints
            constraints = new ArrayList<LinearConstraint>();
            for(Linear L : P.get_constraints().values()){
                double value = L.get_rhs();
                double[] coef = new double[num_unknowns];
                for(int i=0;i<num_unknowns;i++)
                    coef[i] = L.get_coefficient(unknowns.get(i));
                Relationship relationship = ApacheSolver.relation_map.get(L.get_relation());
                constraints.add(new LinearConstraint(coef, relationship, value));
            }

            // add upper bounds
            Iterator ubit = P.get_upper_bounds().entrySet().iterator();
            while (ubit.hasNext()) {
                Map.Entry pairs = (Map.Entry)ubit.next();
                String name = (String) pairs.getKey();
                int ind = unknowns.indexOf(name);
                double[] coef = new double[num_unknowns];
                coef[ind] = 1d;
                Relationship relationship = Relationship.LEQ;
                constraints.add(new LinearConstraint(coef, relationship, (Double) pairs.getValue()));
            }

            // add lower bounds
            Iterator lbit = P.get_lower_bounds().entrySet().iterator();
            while (lbit.hasNext()) {
                Map.Entry pairs = (Map.Entry)lbit.next();
                String name = (String) pairs.getKey();
                int ind = unknowns.indexOf(name);
                double[] coef = new double[num_unknowns];
                coef[ind] = 1d;
                Relationship relationship = Relationship.GEQ;
                constraints.add(new LinearConstraint(coef, relationship, (Double) pairs.getValue()));
            }

            // goal type
            this.goalType = ApacheSolver.opt_map.get(P.get_opt_type());
        }

        public LinearObjectiveFunction get_obj(){
            return obj;
        }
        public ArrayList<LinearConstraint> get_constraints(){
            return constraints;
        }
        public GoalType get_goalType(){
            return goalType;
        }
        public ArrayList<String> get_unknowns(){
            return unknowns;
        }
        public int get_num_unknowns(){
            return num_unknowns;
        }

    }

}
