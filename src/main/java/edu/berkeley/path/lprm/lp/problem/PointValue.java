package edu.berkeley.path.lprm.lp.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gomes on 6/3/14.
 */
public class PointValue {

    public double cost;
    public HashMap<String,Double> name_value = new HashMap<String,Double>();  // variable name -> value

    public PointValue(){}

    public PointValue(ArrayList<String> names,double [] values,double cost){
        if(names.size()!=values.length)
            return;
        this.cost = cost;
        for(int i=0;i<names.size();i++)
            add_value(names.get(i),values[i]);
    }

    public void set_cost(double c){
        this.cost = c;
    }

    public void add_value(String name,double value){
        name_value.put(name,value);
    }

    public double get(String name){
        return name_value.get(name);
    }

    public Double [] get_values(){
        return name_value.values().toArray(new Double[name_value.size()]);
    }

    public String [] get_names(){
        return name_value.keySet().toArray(new String[name_value.size()]);
    }

    @Override
    public String toString() {
        String str = "Cost: " + cost + "\n";
        Iterator it = name_value.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            str += "\t" + pairs.getKey() + " = " + pairs.getValue() + "\n";
        }
        return str;
    }
}
