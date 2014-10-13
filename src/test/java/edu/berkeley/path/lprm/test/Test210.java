package edu.berkeley.path.lprm.test;

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

        double K_dem_seconds = 70;
        double K_cool_seconds = 10;
        double sim_dt_in_seconds = 5;
        double eta = 0.1;

        String config = "C:\\Users\\gomes\\code\\L0\\L0-mpc-demo\\data\\210W_pm_cropped_L0_lp.xml";

        Scenario scenario = ObjectFactory.getScenario(config);

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem, K_cool, eta, sim_dt_in_seconds);
        RampMeteringSolution sol = solver.solve(solver_type);

//        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.matlab);
//        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.text);
//        System.out.println(sol);

        System.out.println(sol.get_cost());

        HashMap<Long,Double[]> profiles= sol.get_metering_profiles_in_vps();
        for (Map.Entry<Long,Double[]> entry : profiles.entrySet()) {
            Double [] c= entry.getValue();
            System.out.println(entry.getKey() + ": " + Arrays.toString(c) );
        }

        assertNotNull(sol);
        //assertTrue(sol.is_ctm());

        System.out.println("done in " + (System.currentTimeMillis()-time));

    }
}
