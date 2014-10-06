package edu.berkeley.path.lprm.lp.problem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by gomes on 6/3/14.
 */
public class Linear {

    private HashMap<String,Double> coefficients = new HashMap<String,Double>();
    private Relation relation = null;
    private double rhs = Double.NaN;

    // construction ..........................................
    public Linear(){}

    // set / add .............................................

    public void set_relation(Relation rel){
        this.relation = rel;
    }

    public void set_rhs(double rhs){
        this.rhs = rhs;
    }

    public void add_coefficient(double value, String name){
        coefficients.put(name,value);
    }

    // get  ..................................................

    public double get_coefficient(String name){
        Double x = coefficients.get(name);
        return x==null ? 0d : x;
    }

    public HashMap<String,Double> get_coefficients(){
        return coefficients;
    }

    public double get_rhs(){
        return rhs;
    }

    public Relation get_relation(){
        return relation;
    }

    public Set<String> get_unknowns(){
        return coefficients.keySet();
    }

    public Problem.ConstraintState evaluate(PointValue P,double epsilon){

        Problem.ConstraintState result = null;
        double lhs = 0d;
        Iterator it = coefficients.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String var_name = (String) pairs.getKey();
            Double alpha = (Double) pairs.getValue();
            lhs += alpha*P.get(var_name);
        }

        double diff = lhs-rhs;
        if(Math.abs(diff)<epsilon)
            return Problem.ConstraintState.active;

        switch(relation){
            case EQ:
                return Problem.ConstraintState.violated;
            case GEQ:
                return diff>epsilon ? Problem.ConstraintState.inactive : Problem.ConstraintState.violated;
            case LEQ:
                return diff<-epsilon ? Problem.ConstraintState.inactive : Problem.ConstraintState.violated;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        String str = "";
        Iterator it = coefficients.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Double value = (Double) pairs.getValue();
            if(value>=0)
                str += " + ";
            else
                str += " - ";
            if(Linear.equals(Math.abs(value),1d))
                str +=  pairs.getKey();
            else
                str +=  String.format("%.2f",Math.abs(value)) + " " + pairs.getKey();
        }
        if(relation!=null){
            switch(relation){
                case LEQ:
                    str += " <= ";
                    break;
                case EQ:
                    str += " = ";
                    break;
                case GEQ:
                    str += " >= ";
                    break;
            }

            if(!Double.isNaN(rhs))
                str += String.format("%.2f",rhs);
        }
        return str;
    }

    public static boolean equals(double x,double y){
        return Math.abs(x-y)<1e-4;
    }

}
