package edu.berkeley.path.lprm.lp.problem;

import java.util.*;

/**
 * Created by gomes on 6/3/14.
 */
public class Problem {

    private HashMap<String,Constraint> constraints = new HashMap<String,Constraint>();
    private Linear cost = new Linear();
    private OptType opt_type = OptType.MIN;

    public Problem(){
    }

    public void setObjective(Linear cst, OptType opttype){
        this.cost = cst;
        this.opt_type = opttype;
    }

    public void add_constraint(Constraint cnst, String name){
        this.constraints.put(name,cnst);
    }

    public void add_upper_bound(String varname, double x){
        Constraint cnst = new Constraint();
        cnst.add_coefficient(1d,varname);
        cnst.set_relation(Relation.LEQ);
        cnst.set_rhs(x);
        cnst.set_type(Constraint.Type.bound);
        constraints.put("UB_"+varname,cnst);
    }

    public void add_lower_bound(String varname, double x){
        Constraint cnst = new Constraint();
        cnst.add_coefficient(1d,varname);
        cnst.set_relation(Relation.GEQ);
        cnst.set_rhs(x);
        cnst.set_type(Constraint.Type.bound);
        constraints.put("LB_"+varname,cnst);
    }

    public void set_constraint_rhs(String name,double value){
        Constraint C = constraints.get(name);
        if(C!=null)
            C.set_rhs(value);
    }

    /** collect unique variable names from cost, constraints, and bounds **/
    public ArrayList<String> get_unique_unknowns(){
        HashSet<String> unique_unknowns = new HashSet<String>();
        unique_unknowns.addAll(cost.get_unknowns());
        for(Linear L : constraints.values())
            unique_unknowns.addAll(L.get_unknowns());
        ArrayList a = new ArrayList(unique_unknowns);
        Collections.sort(a);
        return a;
    }

    public HashMap<String,Constraint> get_constraints(){
        return constraints;
    }

    public Constraint get_constraint(String cnst_name){
        return constraints.get(cnst_name);
    }

    public Double get_lower_bound_for(String varname){
        Constraint cnst = constraints.get("LB_"+varname);
        return cnst==null ? Double.NEGATIVE_INFINITY : cnst.get_rhs();
    }

    public Double get_upper_bound_for(String varname){
        Constraint cnst = constraints.get("UB_"+varname);
        return cnst==null ? Double.POSITIVE_INFINITY : cnst.get_rhs();
    }

    public Linear get_cost(){
        return cost;
    }

    public OptType get_opt_type(){
        return opt_type;
    }

    public Double evaluate_constraint_slack_veh(String cnst_name, PointValue P){
        Constraint cnst = constraints.get(cnst_name);
        if(cnst==null){
            System.out.println("Bad constraint name "+cnst_name);
            return null;
        }
        Double lhs_minus_rhs = cnst.evaluate_lhs_minus_rhs(P);
        switch(cnst.get_relation()){
            case EQ:
                System.out.println("Why am I here?");
                return Math.abs(lhs_minus_rhs);
            case GEQ:
                System.out.println("Why am I here?");
                return lhs_minus_rhs;
            case LEQ:
                return -lhs_minus_rhs;
        }
        return null;
    }

    public double evaluate_cost(PointValue P){
        return cost.evaluate_lhs(P);
    }

    public HashMap<String,Constraint.State> evaluate_constraints(PointValue P,double epsilon){
        HashMap<String,Constraint.State> r = new HashMap<String,Constraint.State>();
        for(Map.Entry<String,Constraint> e : constraints.entrySet())
            r.put( e.getKey() , e.getValue().evaluate_state(P,epsilon) );
        return r;
    }

    public boolean is_feasible(PointValue P,double epsilon,boolean printout){
        boolean feasible = true;
        for(Map.Entry<String,Constraint.State> e : evaluate_constraints(P,epsilon).entrySet()){
            if(e.getValue().compareTo(Constraint.State.violated)==0){
                feasible = false;
                if(printout)
                    System.out.println("Violated: " + e.getKey());
                else
                    break;
            }
        }
        return feasible;
    }

    @Override
    public String toString() {
        TreeMap<String,Constraint> sorted_constraints = new TreeMap<String,Constraint>(constraints);
        String str = opt_type.toString() + ":\n\t" + cost.toString() + "\nSubject to:\n";
        for( Map.Entry<String,Constraint> e : sorted_constraints.entrySet() )
            str += "\t" + e.getKey() + ": " + e.getValue() + "\n";
        return str;
    }

}
