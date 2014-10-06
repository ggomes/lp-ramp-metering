package edu.berkeley.path.lprm.rm;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.lprm.graph.LpNetwork;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.ObjectFactory;

import java.util.ArrayList;

/**
 * Created by gomes on 6/5/14.
 */
public class RampMeteringSolver {

    protected FwyNetwork fwy;
    protected ProblemRampMetering LP;

    ///////////////////////////////////////
    // construction
    ///////////////////////////////////////

    public RampMeteringSolver(String configfile,double K_dem_seconds,double K_cool_seconds, double eta, double sim_dt_in_seconds) throws Exception{
        this(ObjectFactory.getScenario(configfile),
                (int) Math.round(K_dem_seconds / sim_dt_in_seconds),
                (int) Math.round(K_cool_seconds / sim_dt_in_seconds),
                eta, sim_dt_in_seconds);
    }

    public RampMeteringSolver(Scenario scenario, int K_dem, int K_cool, double eta, double sim_dt_in_seconds) throws Exception{
        this( scenario.getNetworkSet().getNetwork().get(0) ,
              scenario.getFundamentalDiagramSet() ,
              scenario.getSplitRatioSet() ,
              scenario.getActuatorSet() ,
              K_dem,K_cool,eta,sim_dt_in_seconds);
    }

    public RampMeteringSolver(Network net, FundamentalDiagramSet fd_set, SplitRatioSet split_ratios, ActuatorSet actuator_set, int K_dem, int K_cool, double eta, double sim_dt_in_seconds) throws Exception{
        fwy = new FwyNetwork(new LpNetwork(net),fd_set,actuator_set);
        fwy.set_split_ratios(split_ratios);
        LP = new ProblemRampMetering(fwy,K_dem,K_cool,eta,sim_dt_in_seconds);
    }

    ///////////////////////////////////////
    // setters / getters
    ///////////////////////////////////////

    public void set_data(InitialDensitySet ic,DemandSet demand_set) throws Exception{
        fwy.set_ic(ic);
        fwy.set_demands(demand_set);
        LP.set_rhs_from_fwy(fwy);
    }

    public void set_density(Long link_id, double density_value){
        fwy.set_density(link_id, density_value);
//        LP.set_rhs_for_link(fwy, link_id);
    }

//    public void set_demand_link_network(Long link_id,Double demand,double demand_dt){
//        fwy.set_demand(link_id, demand, LP.sim_dt_in_seconds);
////        LP.set_rhs_for_link(fwy, link_id);
//    }

//    public void set_rhs_link(Long link_id,Double demand){
//        LP.set_rhs_for_link(fwy,link_id ,demand);
//    }

    public void read_rhs_from_fwy(){
        LP.set_rhs_from_fwy(fwy);
    }

    public FwyNetwork getFwy() {
        return fwy;
    }

    public ArrayList<Long> get_mainline_ids(){
        return fwy.get_mainline_ids();
    }

    public ArrayList<Long> get_metered_onramp_ids(){
        return fwy.get_metered_onramp_ids();
    }

    public ArrayList<String> check_cfl_condition(){
        return fwy.check_cfl_condition(LP.sim_dt_in_seconds);
    }

    ///////////////////////////////////////
    // solve
    ///////////////////////////////////////

    public RampMeteringSolution solve(SolverType solver_type) throws Exception {
        return new RampMeteringSolution(LP,fwy,solver_type);
    }

    ///////////////////////////////////////
    // print
    ///////////////////////////////////////

    public void printLP(){
        System.out.println(LP);
    }

}
