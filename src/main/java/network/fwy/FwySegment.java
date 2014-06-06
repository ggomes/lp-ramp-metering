package network.fwy;

import network.beats.Parameters;
import jaxb.Actuator;
import jaxb.FundamentalDiagram;
import jaxb.Link;
import lp.problem.Linear;

import java.util.ArrayList;
import java.util.List;

public final class FwySegment {

    // link references
    private Long ml_link_id;
    private Long or_link_id;
    private Long fr_link_id;
    private Long fr_node_id;

    // fundamental diagram
    private Double vf;        // [veh/sec] free flow speed
    private Double w;         // [veh/sec] congestion wave speed
    private Double f_max;     // [veh/sec] mainline capacity
    public  Double n_max;     // [veh] mainline jam density
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

    public FwySegment(Link ml_link,Link or_link,Link fr_link, FundamentalDiagram fd, Actuator actuator){

        // link references
        ml_link_id = ml_link==null?null:ml_link.getId();
        or_link_id = or_link==null?null:or_link.getId();
        fr_link_id = fr_link==null?null:fr_link.getId();
        fr_node_id = fr_link==null?null: fr_link.getBegin().getNodeId();
        ml_link_length = ml_link==null?null:ml_link.getLength();
        or_link_length =  or_link==null?null:or_link.getLength();
        or_lanes = or_link==null?null:or_link.getLanes();

        double ml_lanes = ml_link.getLanes();

        // fundamental diagram
        f_max = fd.getCapacity()*ml_lanes;
        vf = fd.getFreeFlowSpeed()/ml_link_length;
        w = fd.getCongestionSpeed()/ml_link_length;
        n_max = fd.getJamDensity()!=null ? fd.getJamDensity() : f_max*(1/vf+1/w);
        n_max *= ml_link_length*ml_lanes;

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

    }

    ///////////////////////////////////////////////////////////////////
    // get
    ///////////////////////////////////////////////////////////////////

    public double get_fmax_vps(){
        return this.f_max;
    }
    public double get_w_vps(){
        return this.w;
    }
    public double get_rmax_vps(){
        return this.r_max;
    }
    public double get_vf_vps(){
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
        double epsilon = 1e-4;
        int k = (int) Math.floor((t+epsilon)/demand_profile_dt);
        return k>=0 && k<demand_profile.size() ? demand_profile.get(k) : 0d;
    }

    public void set_split_ratios(ArrayList<Double> fr_split,double dt){
        split_ratio_profile_dt = dt;
        split_ratio_profile = fr_split;
    }

    // t in seconds
    public double get_split_ratio(double t){
        double epsilon = 1e-4;
        int k = (int) Math.floor((t+epsilon)/split_ratio_profile_dt);
        return k>=0 && k<demand_profile.size() ? demand_profile.get(k) : demand_profile.get(demand_profile.size()-1);
    }

    public void add_to_no_in_vpm(double x){
        no += x*ml_link_length;
    }

    public void add_to_lo_in_vpm(double x){
        lo += x*or_link_length;
    }

    ///////////////////////////////////////////////////////////////////
    // private
    ///////////////////////////////////////////////////////////////////

    private Double get_parameter(Parameters P,String name,Double def){
        if(P==null)
            return def;
        if(!P.has(name))
            return def;
        return Double.parseDouble(P.get(name));
    }

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
