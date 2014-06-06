package test;

import jaxb.*;
import lp.LP_ramp_metering;
import lp.LP_solution;
import network.beats.Network;
import org.junit.Before;
import org.junit.Test;
import beats_link.RampMeteringLimitSet;

public class TestRampMetering {

    private Scenario scenario;

    @Before
    public void setUp() throws Exception {
        scenario = factory.ObjectFactory.getScenario("data/config/210W_v13.xml");
    }

    @Test
    public void testRampMetering() throws Exception {

        LP_ramp_metering problem = new LP_ramp_metering();

        int K_dem = 0;
        int K_cool = 0;
        double sim_dt_in_seconds = Double.NaN;
        problem.set_parameters(K_dem, K_cool,sim_dt_in_seconds);

        Network net = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        problem.set_fwy(net,fds,actuators);

        InitialDensitySet ics = scenario.getInitialDensitySet();
        problem.set_inital_condition(ics);

        DemandSet demands = scenario.getDemandSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        problem.set_boundary_conditions(demands,split_ratios);

        LP_solution sdf = problem.solve();

    }

}