package edu.berkeley.path.lprm.lp.problem;

import edu.berkeley.path.lprm.fwy.FwyStateTrajectory;
import edu.berkeley.path.lprm.rm.ProblemRampMetering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gomes on 6/3/14.
 */
public class PointValue {

//    public double cost;
    public HashMap<String,Double> name_value = new HashMap<String,Double>();  // variable name -> value

    ///////////////////////////////////////////
    // Construction
    ///////////////////////////////////////////

    public PointValue(PointValue x){
//        this.cost = x.cost;
        this.name_value = x.name_value;
    }

    /** cast a freeway state trajectory into a pointvalue **/
    public PointValue(FwyStateTrajectory traj){
        if(traj==null)
            return;
        int i,k;
        int num_segments = traj.fwy.get_num_segments();
        int num_steps = traj.num_steps;
        for(k=0;k<num_steps;k++){
            for(i=0;i<num_segments;i++){
                add_value(ProblemRampMetering.getVar("n",i,k+1),traj.n[i][k+1]);
                add_value(ProblemRampMetering.getVar("f",i,k),traj.f[i][k]);
                if(traj.fwy.get_segment(i).is_metered()){
                    add_value(ProblemRampMetering.getVar("l",i,k+1),traj.l[i][k+1]);
                    add_value(ProblemRampMetering.getVar("r",i,k),traj.r[i][k]);
                }
            }
        }
    }

    public PointValue(ArrayList<String> names,double [] values,double cost){
        if(names.size()!=values.length)
            return;
//        this.cost = cost;
        for(int i=0;i<names.size();i++)
            add_value(names.get(i),values[i]);
    }

    ///////////////////////////////////////////
    // get/set
    ///////////////////////////////////////////

    public HashMap<String,Double> get_name_value(){
        return name_value;
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

//    public double get_cost(){
//        return cost;
//    }

    ///////////////////////////////////////////
    // print
    ///////////////////////////////////////////

    @Override
    public String toString() {
//        String str = "Cost: " + cost + "\n";
        String str = "";
        Iterator it = name_value.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            str += "\t" + pairs.getKey() + " = " + pairs.getValue() + "\n";
        }
        return str;
    }
}
