package lp;

import beats_link.RampMeteringLimitSet;
import beats_link.RampMeteringPolicyMaker;
import beats_link.RampMeteringPolicySet;
import jaxb.*;
import jaxb.Network;
import network.fwy.FwyNetwork;

/**
 * Created by gomes on 6/5/14.
 */
public class RampMeteringLpPolicyMaker implements RampMeteringPolicyMaker {

    protected FwyNetwork fwy;
    protected ProblemRampMetering LP;

    public RampMeteringLpPolicyMaker(network.beats.Network net, FundamentalDiagramSet fd_set, ActuatorSet actuator_set,int K_dem,int K_cool,double eta,double sim_dt_in_seconds) throws Exception{
        fwy = new FwyNetwork(net,fd_set,actuator_set);
        LP = new ProblemRampMetering(fwy,K_dem,K_cool,eta,sim_dt_in_seconds);
    }

    public void set_data(InitialDensitySet ic,DemandSet demand_set,SplitRatioSet split_ratios) throws Exception{
        fwy.set_ic(ic);
        fwy.set_demands(demand_set);
        fwy.set_split_ratios(split_ratios);

        LP.set_rhs_from_fwy(fwy);
    }

    public LP_solution solve() throws Exception {
        return new LP_solution(LP,fwy);
    }

    public void printLP(){
        System.out.println(LP);
    }


    @Override
    public RampMeteringPolicySet givePolicy(Network net, FundamentalDiagramSet fd, DemandSet demand, SplitRatioSet splitRatios, InitialDensitySet ics, RampMeteringLimitSet control, Double dt) {










        return null;
    }

}
