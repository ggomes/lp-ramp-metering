package edu.berkeley.path.lprm.lp;

import edu.berkeley.path.lprm.jaxb.*;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.network.fwy.FwyNetwork;

/**
 * Created by gomes on 6/5/14.
 */
public class RampMeteringSolver {

    protected FwyNetwork fwy;
    protected ProblemRampMetering LP;

    public RampMeteringSolver(edu.berkeley.path.lprm.network.beats.Network net, FundamentalDiagramSet fd_set, SplitRatioSet split_ratios, ActuatorSet actuator_set, int K_dem, int K_cool, double eta, double sim_dt_in_seconds) throws Exception{
        fwy = new FwyNetwork(net,fd_set,actuator_set);
        fwy.set_split_ratios(split_ratios);
        LP = new ProblemRampMetering(fwy,K_dem,K_cool,eta,sim_dt_in_seconds);
    }

    public void set_data(InitialDensitySet ic,DemandSet demand_set) throws Exception{
        fwy.set_ic(ic);
        fwy.set_demands(demand_set);
        LP.set_rhs_from_fwy(fwy);
    }

    public RampMeteringSolution solve(SolverType solver_type) throws Exception {
        return new RampMeteringSolution(LP,fwy,solver_type);
    }

    public void printLP(){
        System.out.println(LP);
    }

    public FwyNetwork getFwy() {
        return fwy;
    }

}
