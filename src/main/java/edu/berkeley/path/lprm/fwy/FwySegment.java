package edu.berkeley.path.lprm.fwy;

import edu.berkeley.path.beats.jaxb.Parameters;
import edu.berkeley.path.beats.jaxb.Parameter;
import edu.berkeley.path.lprm.graph.LpLink;
import edu.berkeley.path.beats.jaxb.Actuator;
import edu.berkeley.path.beats.jaxb.FundamentalDiagram;

import java.util.ArrayList;

public final class FwySegment {

    // link references
    private Long ml_link_id;
    private Long or_link_id;
    private Long fr_link_id;
    private Long fr_node_id;

    // fundamental diagram
    private Double vf;        // [link/sec] free flow speed
    private Double w;         // [link/sec] congestion wave speed
    private Double f_max;     // [veh/sec] mainline capacity
    public  Double n_max;     // [veh/link] mainline jam density
    private Double ml_link_length; // [m] length of the mainline link
    private Double or_link_length; // [m] length of the onramp link
    public Double or_lanes;  // [-] onramp lanes

    // initial condition
    public Double no;        // [veh] initial ml vehicles
    public Double lo;        // [veh] initial or vehicles

    // metering
    public boolean is_metered;
    public Double l_max;     // [veh] maximum or queue length
    private Double r_max;     // [veh/sec] maximum or metering rate

    // data profiles
    private ArrayList<Double> demand_profile;         // [veh/sec] onramp demands
    private double demand_profile_dt;                 // [sec]
    private ArrayList<Double> split_ratio_profile;    // [veh] offramp splits
    private double split_ratio_profile_dt;            // [sec]

    ///////////////////////////////////////////////////////////////////
    // construction
    ///////////////////////////////////////////////////////////////////

    public FwySegment(LpLink ml_link,LpLink or_link,LpLink fr_link, FundamentalDiagram fd, Actuator actuator){

        // link references
        ml_link_id = ml_link==null?null:ml_link.getId();
        or_link_id = or_link==null?null:or_link.getId();
        fr_link_id = fr_link==null?null:fr_link.getId();
        fr_node_id = fr_link==null?null: fr_link.getBegin().getId();
        ml_link_length = ml_link==null?null:ml_link.getLength();
        or_link_length =  or_link==null?null:or_link.getLength();
        or_lanes = or_link==null?null:or_link.getLanes();

        double ml_lanes = ml_link.getLanes();

        // fundamental diagram
        f_max = fd.getCapacity()*ml_lanes;
        vf = fd.getFreeFlowSpeed()/ml_link_length;
        w = fd.getCongestionSpeed()/ml_link_length;
        n_max = fd.getJamDensity()!=null ? fd.getJamDensity() : f_max*(1/vf+1/w);
        n_max *= ml_lanes;

        // metering
        is_metered = actuator!=null;
        if(is_metered){
            Parameters P = (Parameters) actuator.getParameters();
            r_max = get_parameter(P,"max_rate_in_vphpl",Double.POSITIVE_INFINITY)*or_lanes;
            l_max = get_parameter(P,"max_queue_length_in_veh",Double.POSITIVE_INFINITY);
        }
        else{
            r_max = Double.POSITIVE_INFINITY;
            l_max = Double.POSITIVE_INFINITY;
        }

        this.no = 0d;
        this.lo = 0d;

    }

    ///////////////////////////////////////////////////////////////////
    // get
    ///////////////////////////////////////////////////////////////////

    public double get_fmax_vps(){
        return this.f_max;
    }
    public double get_w_link_per_sec(){
        return this.w;
    }
    public double get_rmax_vps(){
        return this.r_max;
    }
    public double get_vf_link_per_sec(){
        return this.vf;
    }

    ///////////////////////////////////////////////////////////////////
    // set
    ///////////////////////////////////////////////////////////////////

    public void reset_state(){
        this.no = 0d;
        this.lo = 0d;
    }

    public void reset_demands(){
        demand_profile = new ArrayList<Double>();
        demand_profile_dt = Double.NaN;
    }

    public void set_demands(ArrayList<Double> demand,double dt){
        demand_profile_dt = dt;
        demand_profile = demand;
    }

    // t in seconds
    public double get_demand_in_vps(double t){
        if(demand_profile==null)
            return 0d;
        double epsilon = 1e-4;
        int k = (int) Math.floor((t+epsilon)/demand_profile_dt);
        return k>=0 && k<demand_profile.size() ?
                demand_profile.get(k) :
                0d;
    }

    public void set_split_ratios(ArrayList<Double> fr_split,double dt){
        split_ratio_profile_dt = dt;
        split_ratio_profile = fr_split;
    }

    // t in seconds
    public double get_split_ratio(double t){
        if(split_ratio_profile==null)
            return 0d;
        double epsilon = 1e-4;
        int k = (int) Math.floor((t+epsilon)/split_ratio_profile_dt);
        return k>=0 && k<split_ratio_profile.size() ?
                split_ratio_profile.get(k) :
                split_ratio_profile.get(split_ratio_profile.size()-1);
    }

    public void add_to_no_in_vpm(double x){
        no += x*ml_link_length;
    }

    public void add_to_lo_in_vpm(double x){
        lo += x*or_link_length;
    }

    ///////////////////////////////////////////////////////////////////
    // parameters
    ///////////////////////////////////////////////////////////////////

    private Double get_parameter(Parameters P,String name,Double def){
        if(P==null)
            return def;
        java.util.ListIterator<Parameter> iter = P.getParameter().listIterator(P.getParameter().size());
        String value = "";
        while (iter.hasPrevious()) {
            Parameter param = iter.previous();
            if (name.equals(param.getName())){
                value = param.getValue();
                break;
            }
        }
        return value.isEmpty() ? Double.NaN : Double.parseDouble(value);
    }

//    private String get(String name) {
//		if(name==null)
//			return null;
//		java.util.ListIterator<edu.berkeley.path.beats.jaxb.Parameter> iter = getParameter().listIterator(getParameter().size());
//		while (iter.hasPrevious()) {
//            edu.berkeley.path.lprm.jaxb.Parameter param = iter.previous();
//			if (name.equals(param.getName())) return param.getValue();
//		}
//		return null;
//	}


    ///////////////////////////////////////////////////////////////////
    // print
    ///////////////////////////////////////////////////////////////////

    @Override
    public String toString() {

        return String.format(  "ml_link_id=%s\n" +
                        "or_link_id=%s\n" +
                        "fr_link_id=%s\n" +
                        "fr_node_id=%s\n" +
                        "ml_link_length=%.1f\n" +
                        "vf=%.1f\n" +
                        "w=%.1f\n" +
                        "f_max=%.1f\n" +
                        "n_max=%.1f\n" +
                        "no=%.1f\n" +
                        "lo=%.1f\n" +
                        "is_metered=%s\n"+
                        "l_max=%.1f\n"+
                        "r_max=%.1f\n"+
                        "dem=%s\n"+
                        "beta=%s\n",
                ml_link_id,
                or_link_id,
                fr_link_id,
                fr_node_id,
                ml_link_length,
                vf,w,f_max,n_max,no,lo,
                is_metered,l_max,r_max,
                demand_profile==null?"[]":demand_profile.toString(),
                split_ratio_profile==null?"[]":split_ratio_profile.toString()
        );
    }

}
