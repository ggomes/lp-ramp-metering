package edu.berkeley.path.lprm.test;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.lprm.ObjectFactory;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;

public class TestSmallNetwork {

    private double sim_dt_in_seconds = 3d;
    private double K_dem_seconds = 36d;
    private double K_cool_seconds = 24d;
    private double eta = .1d;

    private SolverType solver_type = SolverType.LPSOLVE;

    @Test
    public void testSmallNetwork() throws Exception {

        Scenario scenario = ObjectFactory.getScenario("data/config/smallNetwork.xml");

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem, K_cool, eta, sim_dt_in_seconds,solver_type,false);
        RampMeteringSolution sol = solver.solve();

        ArrayList<Long> state_link_ids = new ArrayList<Long>();


//        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.matlab);
        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.text);
//        System.out.println(sol);

//        System.out.println(sol.get_cost());

        assertNotNull(sol);
        //assertTrue(sol.get_max_ctm_distance());
    }
}


