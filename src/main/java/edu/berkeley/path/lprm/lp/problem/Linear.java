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

    // construction ..........................................
    public Linear(){}

    // set / add .............................................
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

    public Set<String> get_unknowns(){
        return coefficients.keySet();
    }

    public Double evaluate_lhs(PointValue P){
        double lhs = 0d;
        Iterator it = coefficients.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String var_name = (String) pairs.getKey();
            Double alpha = (Double) pairs.getValue();
            lhs += alpha*P.get(var_name);
        }
        return lhs;
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
            str +=  String.format("%f",Math.abs(value)) + " " + pairs.getKey();
        }
        return str;
    }

}
