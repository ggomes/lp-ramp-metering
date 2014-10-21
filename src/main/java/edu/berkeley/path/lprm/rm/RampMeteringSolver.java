package edu.berkeley.path.lprm.rm;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.lprm.fwy.FwyStateTrajectory;
import edu.berkeley.path.lprm.graph.LpNetwork;
import edu.berkeley.path.lprm.lp.problem.PointValue;
import edu.berkeley.path.lprm.lp.solver.*;
import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.ObjectFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gomes on 6/5/14.
 */
public class RampMeteringSolver {

    protected FwyNetwork fwy;
    protected ProblemRampMetering LP;
    protected Solver lp_solver;
    protected boolean rhs_in_sync;

    ///////////////////////////////////////
    // construction
    ///////////////////////////////////////

    /** scenario specified in an xml file **/
    public RampMeteringSolver(String configfile,double K_dem_seconds,double K_cool_seconds, double eta, double sim_dt_in_seconds,SolverType solver_type,boolean enforce_constant_splits) throws Exception{
        this(ObjectFactory.getScenario(configfile),
                (int) Math.round(K_dem_seconds / sim_dt_in_seconds),
                (int) Math.round(K_cool_seconds / sim_dt_in_seconds),
                eta, sim_dt_in_seconds,
                solver_type,
                enforce_constant_splits);
    }

    /** scenario given in jaxb object. this constructor is used by beats' RampMeteringPolicyMakerLp **/
    public RampMeteringSolver(Scenario scenario, int K_dem, int K_cool, double eta, double sim_dt_in_seconds,SolverType solver_type,boolean enforce_constant_splits) throws Exception{

        this(scenario.getNetworkSet().getNetwork().get(0) ,
             scenario.getFundamentalDiagramSet() ,
             scenario.getSplitRatioSet() ,
             scenario.getDemandSet() ,
             scenario.getActuatorSet(),
             scenario.getInitialDensitySet() ,
             K_dem,K_cool,eta,sim_dt_in_seconds,
             solver_type,
             enforce_constant_splits);

        // check units
        if(scenario.getSettings().getUnits().compareToIgnoreCase("si")!=0)
            throw new Exception("Scenario units must be SI");
    }

    /** set up lhs structure of the problem from network topology **/
    public RampMeteringSolver(Network network,
                              FundamentalDiagramSet fd_set,
                              SplitRatioSet split_ratios,
                              DemandSet demand_set,
                              ActuatorSet actuator_set,
                              InitialDensitySet id_set,
                              int K_dem,
                              int K_cool,
                              double eta,
                              double sim_dt_in_seconds,
                              SolverType solver_type,
                              boolean enforce_constant_splits) throws Exception{

        // create an lp solver
        switch(solver_type){
            case APACHE:
                lp_solver = new ApacheSolver();
                break;
            case LPSOLVE:
                lp_solver = new LpSolveSolver();
                break;
            case GUROBI:
                lp_solver = new GurobiSolver();
                break;
        }

        // create the freeway object
        fwy = new FwyNetwork(new LpNetwork(network),fd_set,actuator_set);
        fwy.set_split_ratios(split_ratios,enforce_constant_splits);
        fwy.set_ic(id_set);
        fwy.set_demands(demand_set);

        // check CFL
        String errors = fwy.check_cfl_condition(sim_dt_in_seconds);
        if (!errors.isEmpty())
            throw new Exception("CFL error\n"+errors);

        // create the lp object
        LP = new ProblemRampMetering(fwy,K_dem,K_cool,eta,sim_dt_in_seconds);

        // translate into solver
        lp_solver.initialize(LP);

        rhs_in_sync = true;
    }

    ///////////////////////////////////////
    // setters / getters
    ///////////////////////////////////////

    /** transfer information from fwy to LP and lp_solver **/
    public void sync_rhs(){
        if(rhs_in_sync)
            return;
        LP.set_rhs_from_fwy(fwy);
        lp_solver.set_constraint_rhs(LP);
        rhs_in_sync = true;
    }

    public void set_data(InitialDensitySet ic,DemandSet demand_set) throws Exception{
        fwy.set_ic(ic);
        fwy.set_demands(demand_set);
        rhs_in_sync = false;
    }

    public void set_density_in_vpm(Long link_id, double density_value) throws Exception {
        fwy.set_density_in_vpm(link_id, density_value);
        rhs_in_sync = false;
    }

    public void set_density_in_veh(Long link_id, double density_value) throws Exception {
        fwy.set_density_in_veh(link_id, density_value);
        rhs_in_sync = false;
    }

    public void set_demand_in_vps(Long or_link_id, Double demand) throws Exception {
        fwy.set_demand_in_vps(or_link_id, demand);
        rhs_in_sync = false;
    }

    public FwyNetwork getFwy() {
        return fwy;
    }

    public ProblemRampMetering getLP(){
        return LP;
    }

    public ArrayList<Long> get_mainline_ids(){
        return fwy.get_mainline_ids();
    }

    public ArrayList<Long> get_metered_onramp_ids(){
        return fwy.get_metered_onramp_ids();
    }

    ///////////////////////////////////////
    // solve
    ///////////////////////////////////////

    public RampMeteringSolution solve() throws Exception {
        RampMeteringSolution sol;
        double ctm_distance = Double.POSITIVE_INFINITY;
        double ctm_threshold = 1d;
        FwyStateTrajectory warm_start = null;
        int count = 0;
        int max_count = 10;

        // make sure everything is in sync
        sync_rhs();

        // solution loop
        while(count<max_count){

            // call the solver using a warm_start
            PointValue pv = lp_solver.solve(warm_start);

            // cast as a RampMeteringSolution
            sol = new RampMeteringSolution(LP,fwy,pv);

            // update ctm distance
            ctm_distance = sol.get_max_ctm_distance();

            // break condition
            if(ctm_distance<ctm_threshold)
                break;

            // extract optimal ramp flows
            HashMap<Long,double[]> opt_r = sol.get_ramp_flow_in_veh();

            // simulate, obtain warm start for next iteration
            warm_start = fwy.simulate(LP.sim_dt_in_seconds, LP.K_dem, LP.K_cool, opt_r);

            count++;
        }

        return sol;
    }

    ///////////////////////////////////////
    // print
    ///////////////////////////////////////

    public void printLP(){
        System.out.println(LP);
    }

    public void print_solver_lp(){
        System.out.println(lp_solver);
    }

}
