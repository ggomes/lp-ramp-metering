package edu.berkeley.path.lprm.test;

import edu.berkeley.path.beats.jaxb.Density;
import edu.berkeley.path.beats.jaxb.InitialDensitySet;
import edu.berkeley.path.beats.jaxb.Link;
import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.lprm.ObjectFactory;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


public class Test210 {

    private SolverType solver_type = SolverType.GUROBI;

    @Test
    public void test210() throws Exception {

        long time = System.currentTimeMillis();

        double K_dem_seconds = 3600;
        double K_cool_seconds = 900;
        double sim_dt_in_seconds = 5;
        double eta = 0.1;
        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);
        String config = "C:\\Users\\gomes\\code\\L0\\L0-mpc-demo\\data\\210W_pm_cropped_L0_lp.xml";

        Scenario scenario = ObjectFactory.getScenario(config);

        // set initial condition
//        InitialDensitySet ids = new InitialDensitySet();
//        for(Link L : scenario.getNetworkSet().getNetwork().get(0).getLinkList().getLink()){
//            Density d = new Density();
//            d.setLinkId(L.getId());
//            if(L.getId()==58)
//                d.setContent("0.0");
//            else
//                d.setContent("0.0");
//            ids.getDensity().add(d);
//        }
//        scenario.setInitialDensitySet(ids);

        // create solver and solve
        RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem, K_cool, eta, sim_dt_in_seconds,false);
        RampMeteringSolution sol = solver.solve(solver_type);

        sol.print_to_file("C:\\Users\\gomes\\code\\L0\\L0-mpc-demo\\out\\210test", RampMeteringSolution.OutputFormat.matlab);
        System.out.println("Distance to CTM: " + sol.get_max_ctm_distance() + "\t" + sol.get_leftover_vehicles());

//        HashMap<Long,Double[]> profiles= sol.get_metering_profiles_in_vps();
//        for (Map.Entry<Long,Double[]> entry : profiles.entrySet()) {
//            Double [] c= entry.getValue();
//            System.out.println(entry.getKey() + ": " + Arrays.toString(c) );
//        }

        assertNotNull(sol);
        //assertTrue(sol.is_ctm());

        System.out.println("done in " + (System.currentTimeMillis()-time));

    }
}
