package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import org.junit.Test;

/**
 * Created by gomes on 11/24/2014.
 */
public class Test_rm_AB {


    @Test
    public void test_rm_A() throws Exception {

        String config_file = "data/config/test_rm_C.xml";
        double sim_dt_in_seconds = 3d;
        double K_dem_seconds = 36d;
        double K_cool_seconds = 24d;
        double eta = .1d;

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        RampMeteringSolver solver = new RampMeteringSolver(config_file,
                K_dem,
                K_cool,
                eta,
                sim_dt_in_seconds,
                SolverType.GUROBI,
                false);

        RampMeteringSolution result = solver.solve();

        System.out.println(result);

    }


}