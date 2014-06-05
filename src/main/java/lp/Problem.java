package lp;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.linear.LinearConstraint;
import org.apache.commons.math3.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optimization.linear.Relationship;
import org.apache.commons.math3.optimization.linear.SimplexSolver;

import java.util.*;

/**
 * Created by gomes on 6/3/14.
 */
public class Problem {

    public static HashMap<Relation,Relationship> relation_map = new HashMap<Relation,Relationship>();
    public static HashMap<OptType,GoalType> opt_map = new HashMap<OptType,GoalType>();
    static {
        relation_map.put(Relation.EQ  , Relationship.EQ);
        relation_map.put(Relation.LEQ , Relationship.LEQ);
        relation_map.put(Relation.GEQ , Relationship.GEQ);
        opt_map.put(OptType.MAX,GoalType.MAXIMIZE);
        opt_map.put(OptType.MIN,GoalType.MINIMIZE);
    }

    public HashMap<String,Linear> constraints = new HashMap<String,Linear>();
    public ArrayList<Linear> bounds = new ArrayList <Linear>();
    public Linear cost = new Linear();
    public OptType opt_type = OptType.MIN;

    public Problem(){
    }

    public void setObjective(Linear cst, OptType opttype){
        this.cost = cst;
        this.opt_type = opttype;
    }

    public void add_constraint(Linear linear, String name){
        this.constraints.put(name,linear);
    }

    public void add_bound(String name,Relation relation,double x){
        Linear bound = new Linear();
        bound.add_coefficient(1d, name);
        bound.set_relation(relation);
        bound.set_rhs(x);
        bounds.add(bound);
    }

    /** collect unique variable names from cost, constraints, and bounds **/
    public String [] get_unique_unknowns(){
        HashSet<String> unique_unknowns = new HashSet<String>();
        unique_unknowns.addAll(cost.get_unknowns());
        for(Linear L : bounds)
            unique_unknowns.addAll(L.get_unknowns());
        for(Linear L : constraints.values())
            unique_unknowns.addAll(L.get_unknowns());
        return unique_unknowns.toArray(new String[unique_unknowns.size ()]);
    }


    @Override
    public String toString() {
        String str = "";
        switch(opt_type){
            case MAX:
                str += "Maximize:\n";
                break;
            case MIN:
                str += "Minimize:\n";
                break;
        }
        str += "\t" + cost.toString() + "\n";
        str += "Subject to:\n";
        Iterator cit = constraints.entrySet().iterator();
        while (cit.hasNext()) {
            Map.Entry pairs = (Map.Entry)cit.next();
            str += "\t" + pairs.getKey() + ": " + pairs.getValue() + "\n";
        }
        str += "With bounds:\n";
        for(Linear L : bounds)
            str += "\t" + L + "\n";
        return str;
    }
}
