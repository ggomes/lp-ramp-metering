package test;

import jaxb.*;
import lp.LP_solution;
import lp.RampMeteringLpPolicyMaker;
import network.beats.Network;
import org.junit.Before;
import org.junit.Test;

public class TestRampMetering {

    private Scenario scenario;

    @Before
    public void setUp() throws Exception {
        scenario = factory.ObjectFactory.getScenario("data/config/210W_v13.xml");
    }

    @Test
    public void testRampMetering() throws Exception {

        int K_dem = 0;
        int K_cool = 0;
        double eta = .1d;
        double sim_dt_in_seconds = Double.NaN;

        Network net = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        RampMeteringLpPolicyMaker policy_maker = new RampMeteringLpPolicyMaker(net,fds,actuators,K_dem,K_cool,eta,sim_dt_in_seconds);

        InitialDensitySet ics = scenario.getInitialDensitySet();
        policy_maker.set_inital_condition(ics);

        DemandSet demands = scenario.getDemandSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        policy_maker.set_boundary_conditions(demands,split_ratios);

        LP_solution sdf = policy_maker.solve();

    }

}