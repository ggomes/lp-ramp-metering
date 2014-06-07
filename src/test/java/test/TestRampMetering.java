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
        scenario = factory.ObjectFactory.getScenario("data/config/_smalltest_MPC_SI.xml");
    }

    @Test
    public void testRampMetering() throws Exception {

        int K_dem = 5;
        int K_cool = 5;
        double eta = .1d;
        double sim_dt_in_seconds = 5;

        Network net = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        RampMeteringLpPolicyMaker policy_maker = new RampMeteringLpPolicyMaker(net,fds,actuators,K_dem,K_cool,eta,sim_dt_in_seconds);

        InitialDensitySet ics = scenario.getInitialDensitySet();
        DemandSet demands = scenario.getDemandSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        policy_maker.set_data(ics,demands,split_ratios);


        policy_maker.printLP();

        LP_solution sol = policy_maker.solve();

    }

}