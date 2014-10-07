package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.lprm.ObjectFactory;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class TestSmallNetwork {

    private double sim_dt_in_seconds = 3d;
    private double K_dem_seconds = 12d;
    private double K_cool_seconds = 12d;
    private double eta = .1d;

    private SolverType solver_type = SolverType.LPSOLVE;

    @Test
    public void testSmallNetwork() throws Exception {

        Scenario scenario = ObjectFactory.getScenario("data/config/smallNetwork.xml");

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem, K_cool, eta, sim_dt_in_seconds);
        RampMeteringSolution sol = solver.solve(solver_type);

//        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.matlab);
//        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.text);
//        System.out.println(sol);

        System.out.println(sol.get_cost());

        assertNotNull(sol);
        assertTrue(sol.is_ctm());
    }
}


