package edu.berkeley.path.lprm.lp.problem;

import java.util.*;

/**
 * Created by gomes on 6/3/14.
 */
public class Problem {

    public enum ConstraintState {violated,active,inactive};

    private HashMap<String,Linear> constraints = new HashMap<String,Linear>();
    private HashMap<String,Double> upper_bounds = new HashMap<String,Double>();
    private HashMap<String,Double> lower_bounds = new HashMap<String,Double>();
    private Linear cost = new Linear();
    private OptType opt_type = OptType.MIN;

    public Problem(){
    }

    public void setObjective(Linear cst, OptType opttype){
        this.cost = cst;
        this.opt_type = opttype;
    }

    public void add_constraint(Linear linear, String name){
        this.constraints.put(name,linear);
    }

    public void add_upper_bound(String varname, double x){
        upper_bounds.put(varname,x);
    }

    public void add_lower_bound(String varname, double x){
        lower_bounds.put(varname,x);
    }

    public void set_constraint_rhs(String name,double value){
        Linear C = constraints.get(name);
        if(C!=null)
            C.set_rhs(value);
    }

    /** collect unique variable names from cost, constraints, and bounds **/
    public ArrayList<String> get_unique_unknowns(){
        HashSet<String> unique_unknowns = new HashSet<String>();
        unique_unknowns.addAll(cost.get_unknowns());
        unique_unknowns.addAll(upper_bounds.keySet());
        unique_unknowns.addAll(lower_bounds.keySet());
        for(Linear L : constraints.values())
            unique_unknowns.addAll(L.get_unknowns());
        ArrayList a = new ArrayList(unique_unknowns);
        Collections.sort(a);
        return a;
    }

    public HashMap<String,Linear> get_constraints(){
        return constraints;
    }

    public HashMap<String,Double> get_upper_bounds(){
        return upper_bounds;
    }

    public HashMap<String,Double> get_lower_bounds(){
        return lower_bounds;
    }

    public Linear get_cost(){
        return cost;
    }

    public OptType get_opt_type(){
        return opt_type;
    }

    public HashMap<String,ConstraintState> evaluate_upper_bounds(PointValue P,double epsilon) {
        HashMap<String,ConstraintState> result = new HashMap<String,ConstraintState>();

        Iterator it = upper_bounds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String var_name = (String) pairs.getKey();
            Double rhs = (Double) pairs.getValue();
            Double lhs = P.get(var_name);
            ConstraintState r = null;

            if(Math.abs(rhs-lhs)<epsilon)
                r = ConstraintState.active;
            else if(lhs>rhs+epsilon)
                r = ConstraintState.violated;
            else
                r = ConstraintState.inactive;
//            if(rhs)

//            ConstraintState lin_state = cnst.evaluate(P,epsilon);
            result.put(var_name,r);
        }
        return result;
    }

    public HashMap<String,ConstraintState> evaluate_lower_bounds(PointValue P,double epsilon) {

        HashMap<String,ConstraintState> result = new HashMap<String,ConstraintState>();

        Iterator it = lower_bounds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String var_name = (String) pairs.getKey();
            Double rhs = (Double) pairs.getValue();
            Double lhs = P.get(var_name);
            ConstraintState r = null;

            if(Math.abs(rhs-lhs)<epsilon)
                r = ConstraintState.active;
            else if(lhs<rhs-epsilon)
                r = ConstraintState.violated;
            else
                r = ConstraintState.inactive;
//            if(rhs)

//            ConstraintState lin_state = cnst.evaluate(P,epsilon);
            result.put(var_name,r);
        }
        return result;

    }

    public HashMap<String,ConstraintState> evaluate_constraints(PointValue P,double epsilon) {
        HashMap<String,ConstraintState> result = new HashMap<String,ConstraintState>();
        Iterator it = constraints.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String cnst_name = (String) pairs.getKey();
            Linear cnst = (Linear) pairs.getValue();
            ConstraintState lin_state = cnst.evaluate(P,epsilon);
            result.put(cnst_name,lin_state);
        }
        return result;
    }

    @Override
    public String toString() {

        TreeMap<String,Linear> sorted_constraints = new TreeMap<String,Linear>(constraints);
        TreeMap<String,Double> sorted_upper_bounds = new TreeMap<String,Double>(upper_bounds);
        TreeMap<String,Double> sorted_lower_bounds = new TreeMap<String,Double>(lower_bounds);

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
        Iterator cit = sorted_constraints.entrySet().iterator();
        while (cit.hasNext()) {
            Map.Entry pairs = (Map.Entry)cit.next();
            str += "\t" + pairs.getKey() + ": " + pairs.getValue() + "\n";
        }
        str += "Upper bounds:\n";
        Iterator ubit = sorted_upper_bounds.entrySet().iterator();
        while (ubit.hasNext()) {
            Map.Entry pairs = (Map.Entry)ubit.next();
            str += "\t" + pairs.getKey() + " <= " + pairs.getValue() + "\n";
        }
        str += "Lower bounds:\n";
        Iterator lbit = sorted_lower_bounds.entrySet().iterator();
        while (lbit.hasNext()) {
            Map.Entry pairs = (Map.Entry)lbit.next();
            str += "\t" + pairs.getKey() + " >= " + pairs.getValue() + "\n";
        }
        return str;
    }
}
