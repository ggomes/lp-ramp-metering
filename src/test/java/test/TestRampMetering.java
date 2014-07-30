package test;

import jaxb.*;
import lp.RampMeteringSolution;
import lp.RampMeteringLpPolicyMaker;
import lp.solver.SolverType;
import network.beats.Network;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestRampMetering {

    private Scenario scenario;

    @Before
    public void setUp() throws Exception {
        String config = "data/config/smallNetwork.xml";

        scenario = factory.ObjectFactory.getScenario(config);
    }

    @Test
    public void testRampMetering() throws Exception {

        double sim_dt_in_seconds = 5;
        int K_dem = (int) Math.round(5/sim_dt_in_seconds);
        int K_cool = (int) Math.round(5/sim_dt_in_seconds);
        double eta = .1d;

        Network net = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        RampMeteringLpPolicyMaker policy_maker = new RampMeteringLpPolicyMaker(net,fds,actuators,K_dem,K_cool,eta,sim_dt_in_seconds);

        InitialDensitySet ics = scenario.getInitialDensitySet();
        DemandSet demands = scenario.getDemandSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        policy_maker.set_data(ics,demands,split_ratios);

        policy_maker.printLP();

        RampMeteringSolution sol = policy_maker.solve(SolverType.APACHE);

        System.out.println("\n\nSOLVED:\n"+sol.print(true));
        assertNotNull(sol);

    }

}