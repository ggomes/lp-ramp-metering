package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.lp.RampMeteringSolution;
import edu.berkeley.path.lprm.jaxb.*;
import edu.berkeley.path.lprm.factory.ObjectFactory;
import edu.berkeley.path.lprm.lp.RampMeteringSolver;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.network.beats.Network;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestRampMetering {

    private Scenario scenario;

    @Before
    public void setUp() throws Exception {
        String config = "data/config/smallNetwork.xml";

        scenario = ObjectFactory.getScenario(config);
    }

    @Test
    public void testRampMetering() throws Exception {

        double sim_dt_in_seconds = 3d;
        int K_dem = (int) Math.round(9/sim_dt_in_seconds);
        int K_cool = (int) Math.round(3/sim_dt_in_seconds);
        double eta = .1d;

//        double sim_dt_in_seconds = 5;
//        int K_dem = (int) Math.round(5/sim_dt_in_seconds);
//        int K_cool = (int) Math.round(5/sim_dt_in_seconds);
//        double eta = .1d;
//

        Network net = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        RampMeteringSolver solver = new RampMeteringSolver(net,fds,split_ratios,actuators,K_dem,K_cool,eta,sim_dt_in_seconds);

        InitialDensitySet ics = scenario.getInitialDensitySet();
        DemandSet demands = scenario.getDemandSet();
        solver.set_data(ics, demands);

        solver.printLP();

        RampMeteringSolution sol = solver.solve(SolverType.APACHE);

        System.out.println("\n\nSOLVED:\n"+sol);
        //sol.print_to_file("SmallNetwork.txt");




        assertNotNull(sol);

    }

}