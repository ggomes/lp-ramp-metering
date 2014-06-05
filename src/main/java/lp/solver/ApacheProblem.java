package lp.solver;

import lp.problem.Linear;
import lp.problem.Problem;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.linear.LinearConstraint;
import org.apache.commons.math3.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optimization.linear.Relationship;

import java.util.ArrayList;

/**
 * Created by gomes on 6/4/14.
 */
public class ApacheProblem {

    private String []  unknowns;
    private int num_unknowns;
    private LinearObjectiveFunction obj;
    private ArrayList<LinearConstraint> constraints;
    private GoalType goalType;

    public ApacheProblem(Problem P){

        unknowns = P.get_unique_unknowns();
        num_unknowns = unknowns.length;

        // cost function
        double[] coefficients = new double[num_unknowns];
        for(int i=0;i<num_unknowns;i++)
            coefficients[i] = P.cost.get_coefficient(unknowns[i]);
        this.obj = new LinearObjectiveFunction(coefficients,0d);

        // constraints
        constraints = new ArrayList<LinearConstraint>();
        for(Linear L : P.constraints.values()){
            double value = L.get_rhs();
            double[] coef = new double[num_unknowns];
            for(int i=0;i<num_unknowns;i++)
                coef[i] = L.get_coefficient(unknowns[i]);
            Relationship relationship = P.relation_map.get(L.get_relation());
            constraints.add(new LinearConstraint(coef, relationship, value));
        }

        // add bounds to constraints (FIX THIS!!)
        for(Linear L : P.bounds){
            double value = L.get_rhs();
            double[] coef = new double[num_unknowns];
            for(int i=0;i<num_unknowns;i++)
                coef[i] = L.get_coefficient(unknowns[i]);
            Relationship relationship = P.relation_map.get(L.get_relation());
            constraints.add(new LinearConstraint(coef, relationship, value));
        }

        // goal type
        this.goalType = P.opt_map.get(P.opt_type);
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
    public String [] get_unknowns(){
        return unknowns;
    }
    public int get_num_unknowns(){
        return num_unknowns;
    }

}
