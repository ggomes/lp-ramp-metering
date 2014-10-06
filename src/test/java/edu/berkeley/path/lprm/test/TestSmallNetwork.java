package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.lprm.ObjectFactory;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class TestSmallNetwork {

    private Scenario scenario;
//    private double sim_dt_in_seconds = 2d;
//    private double K_dem_seconds     = 10d;
//    private double K_cool_seconds    = 8d;
//    private double eta = .1d;

    private double sim_dt_in_seconds = 3d;
    private double K_dem_seconds = 9d;
    private double K_cool_seconds = 3d;
    private double eta = .1d;

    private SolverType solver_type = SolverType.APACHE;

    @Before
    public void setUp() throws Exception {
        String config = "data/config/smallNetwork.xml";
        scenario = ObjectFactory.getScenario(config);
    }

    @Test
    public void testSmallNetwork() throws Exception {

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        Network net = scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        RampMeteringSolver solver = new RampMeteringSolver(net, fds, split_ratios, actuators, K_dem, K_cool, eta, sim_dt_in_seconds);

        ArrayList<String> errors = solver.getFwy().check_cfl_condition(sim_dt_in_seconds);
        if (!errors.isEmpty()) {
            System.err.print(errors);
            throw new Exception("CFL error");
        }
        InitialDensitySet ics = scenario.getInitialDensitySet();
        DemandSet demands = scenario.getDemandSet();
        solver.set_data(ics, demands);
        RampMeteringSolution sol = solver.solve(solver_type);

        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.matlab);
        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.text);
        System.out.println(sol);

        assertNotNull(sol);
        assertTrue(sol.is_ctm());
    }
}


