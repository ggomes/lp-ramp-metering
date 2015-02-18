package edu.berkeley.path.lprm.lp.problem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
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

    public void write_to_files(String prefix){

        PrintWriter unknowns_file = null;
        PrintWriter cost_file = null;
        PrintWriter eq_names_file = null;
        PrintWriter eq_lhs_file = null;
        PrintWriter eq_rhs_file = null;
        PrintWriter iq_names_file = null;
        PrintWriter iq_lhs_file = null;
        PrintWriter iq_rhs_file = null;
//        PrintWriter lb_file = null;
//        PrintWriter ub_file = null;
        try {

            // open files
            unknowns_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_unknowns.txt")));
            cost_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_cost.txt")));
            eq_names_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_eq_names.txt")));
            eq_lhs_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_eq_lhs.txt")));
            eq_rhs_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_eq_rhs.txt")));
            iq_names_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_iq_names.txt")));
            iq_lhs_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_iq_lhs.txt")));
            iq_rhs_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_iq_rhs.txt")));
//            lb_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_lb.txt")));
//            ub_file = new PrintWriter(new BufferedWriter(new FileWriter(prefix + "_ub.txt")));

            // write unknowns and cost
            ArrayList<String> unknowns = get_unique_unknowns();
            for(String unknown : unknowns) {
                unknowns_file.print(unknown);
                cost_file.print(cost.get_coefficient(unknown)+"\t");
            }

            // write constraints
            TreeMap<String,Constraint> sorted_constraints = new TreeMap<String,Constraint>(constraints);
            for(Map.Entry<String,Constraint> entry : sorted_constraints.entrySet()) {
                String cnst_name = entry.getKey();
                Constraint cnst = entry.getValue();
                Relation relation = cnst.get_relation();
                if(relation!=null) {
                    switch (relation) {
                        case LEQ:
                            iq_names_file.println(cnst_name);
                            for(String unknown : unknowns)
                                iq_lhs_file.print(cnst.get_coefficient(unknown) + "\t");
                            iq_lhs_file.print("\n");
                            iq_rhs_file.println(cnst.get_rhs());
                            break;
                        case EQ:
                            eq_names_file.println(cnst_name);
                            for(String unknown : unknowns)
                                eq_lhs_file.print(cnst.get_coefficient(unknown) + "\t");
                            eq_lhs_file.print("\n");
                            eq_rhs_file.println(cnst.get_rhs());
                            break;
                        case GEQ:
                            iq_names_file.println(cnst_name);
                            for(String unknown : unknowns)
                                iq_lhs_file.print(-cnst.get_coefficient(unknown) + "\t");
                            iq_lhs_file.print("\n");
                            iq_rhs_file.println(-cnst.get_rhs());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unknowns_file.close();
            cost_file.close();
            eq_names_file.close();
            eq_lhs_file.close();
            eq_rhs_file.close();
            iq_names_file.close();
            iq_lhs_file.close();
            iq_rhs_file.close();
//                lb_file.close();
//                ub_file.close();
        }
    }

}
