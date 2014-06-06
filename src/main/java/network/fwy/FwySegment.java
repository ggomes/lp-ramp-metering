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
    public Long ml_link_id;
    public Long or_link_id;
    public Long fr_link_id;
    public Long fr_node_id;

    // fundamental diagram
    public Double vf;        // [veh] free flow speed
    public Double w;         // [veh] congestion wave speed
    public Double f_max;     // [veh] mainline capacity
    public Double n_max;     // [veh] mainline jam density
    public Double ml_link_length; // [m] length of the mainline link
    public Double or_link_length; // [m] length of the onramp link
    public Double or_lanes;  // [-] onramp lanes

    // initial condition
    public Double no;        // [veh] initial ml vehicles
    public Double lo;        // [veh] initial or vehicles

    // metering
    public boolean is_metered;
    public Double l_max;     // [veh] maximum or queue length
    public Double r_max;     // [veh] maximum or meterign rate

    // constraints
//    public List<Linear> ORcons = new ArrayList<Linear>();
//    public List<Linear> MLcng = new ArrayList<Linear>();
//    public List<Linear> ORdem = new ArrayList<Linear>();

    // data profiles
    public ArrayList<Double> demand_profile;         // [veh] onramp demands
    public ArrayList<Double> split_ratio_profile;    // [veh] offramp splits

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
        f_max = fd.getCapacity();
        f_max *= sim_dt_in_seconds*ml_lanes;

        vf = fd.getFreeFlowSpeed();
        vf *= sim_dt_in_seconds/ml_link_length;

        w = fd.getCongestionSpeed();
        w *= sim_dt_in_seconds/ml_link_length;

        n_max = fd.getJamDensity()!=null ? fd.getJamDensity() : f_max*(1/vf+1/w);
        n_max *= ml_link_length*ml_lanes;

        // metering
        is_metered = actuator!=null;
        if(is_metered){
            Parameters P = (Parameters) actuator.getParameters();
            r_max = get_parameter(P,"max_rate_in_vphpl",Double.POSITIVE_INFINITY)*or_lanes*sim_dt_in_seconds;
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

    public double d(int k){
        try{
            return demand_profile.get(k);
        } catch(Exception e){
            return 0d;
        }
    }

    public double betabar(int k){
        try{
            return 1-split_ratio_profile.get(k);
        } catch(Exception e){
            return 1d;
        }
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
