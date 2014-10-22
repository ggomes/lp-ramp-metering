package edu.berkeley.path.lprm.test;

import edu.berkeley.path.beats.jaxb.Density;
import edu.berkeley.path.beats.jaxb.InitialDensitySet;
import edu.berkeley.path.beats.jaxb.Link;
import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.lprm.ObjectFactory;
import edu.berkeley.path.lprm.fwy.FwyStateTrajectory;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;


public class Test210 {

    private SolverType solver_type = SolverType.GUROBI;

    @Test
    public void test210() throws Exception {

        long time = System.currentTimeMillis();

        double K_dem_seconds = 3600;
        double K_cool_seconds = 600;
        double sim_dt_in_seconds = 5;
        double eta = 0.1d;
        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);
        String config = "C:\\Users\\gomes\\code\\L0\\L0-mpc-demo\\data\\210W_pm_cropped_L0_lp.xml";

        Scenario scenario = ObjectFactory.getScenario(config);

        // set initial condition
        InitialDensitySet ids = new InitialDensitySet();
        for(Link L : scenario.getNetworkSet().getNetwork().get(0).getLinkList().getLink()){
            Density d = new Density();
            d.setLinkId(L.getId());
            d.setContent("0.05");
            ids.getDensity().add(d);
        }
        scenario.setInitialDensitySet(ids);

        // create solver and solve
        RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem, K_cool, eta, sim_dt_in_seconds,solver_type,true);
        RampMeteringSolution sol = solver.solve();

        System.out.println("Feasible: " + solver.getLP().is_feasible(sol,1e-2,true));
        System.out.println("Distance to CTM: " + sol.get_max_ctm_distance());
        System.out.println("Leftover vehicles: " + sol.get_leftover_vehicles());

        assertNotNull(sol);

        System.out.println("done in " + (System.currentTimeMillis()-time));

    }
}
