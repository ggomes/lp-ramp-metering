package edu.berkeley.path.lprm.lp.solver;

import edu.berkeley.path.lprm.lp.problem.*;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.linear.LinearConstraint;
import org.apache.commons.math3.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optimization.linear.Relationship;
import org.apache.commons.math3.optimization.linear.SimplexSolver;

import java.util.ArrayList;
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
    private ApacheProblem problem;
    private SimplexSolver solver;

    @Override
    public void initialize(Problem P) {
        problem = new ApacheProblem(P);
        solver = new SimplexSolver();
    }

    @Override
    public PointValue solve() {
        PointValuePair pair = solver.optimize(
                problem.get_obj(),
                problem.get_constraints(),
                problem.get_goalType(),
                false );
        return new PointValue(problem.get_unknowns(),pair.getPoint(),pair.getValue());
    }

    @Override
    public String toString() {
        return problem.toString();
    }

    @Override
    public void set_constraint_rhs(Problem P) {
        System.err.println("NOT IMPLEMENTED!");
    }

    private class ApacheProblem {

        private ArrayList<String> unknowns;
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
            for(Constraint L : P.get_constraints().values()){
                double value = L.get_rhs();
                double[] coef = new double[num_unknowns];
                for(int i=0;i<num_unknowns;i++)
                    coef[i] = L.get_coefficient(unknowns.get(i));
                Relationship relationship = ApacheSolver.relation_map.get(L.get_relation());
                constraints.add(new LinearConstraint(coef, relationship, value));
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

        @Override
        public String toString() {
            String str = "";
            str += goalType + " ";
            RealVector coef = obj.getCoefficients();
            for(int i=0;i<num_unknowns;i++)
                str += (coef.getEntry(i)>=0 ? " +" : " ") + coef.getEntry(i) + " " + unknowns.get(i);
            str += "\nSubject to:\n";
            for(LinearConstraint cnst : constraints){
                for(int i=0;i<num_unknowns;i++){
                    coef = cnst.getCoefficients();
                    if(coef.getEntry(i)!=0d){
                        str += (coef.getEntry(i)>=0 ? " +" : " ") + coef.getEntry(i) + " " + unknowns.get(i);
                    }
                }
                str += " " + cnst.getRelationship() + " " + cnst.getValue() + "\n";
            }
            return str;
        }
    }

}
