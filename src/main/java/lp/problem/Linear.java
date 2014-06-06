package lp.problem;

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

    public Linear(){

    }

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

//    public boolean is_valid_cost(){
//        return !coefficients.isEmpty();
//    }
//
//    public boolean is_valid_bound(){
//        return !coefficients.isEmpty() && relation!=null;
//    }
//
//    public boolean is_valid_constraint(){
//        return !coefficients.isEmpty() && relation!=null && !Double.isNaN(rhs);
//    }

    public double get_coefficient(String name){
        Double x = coefficients.get(name);
        return x==null ? 0d : x;
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

    @Override
    public String toString() {
        String str = "";
        Iterator it = coefficients.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            str +=  pairs.getValue() + " " + pairs.getKey();
            if(it.hasNext())
                str += " + ";
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
                str += rhs;
        }
        return str;
    }
}
