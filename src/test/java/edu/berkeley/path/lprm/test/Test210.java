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

        double K_dem_seconds = 300;
        double K_cool_seconds = 300;
        double sim_dt_in_seconds = 5;
        double eta = 10d;
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
        RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem, K_cool, eta, sim_dt_in_seconds,true);
        RampMeteringSolution sol = solver.solve(solver_type);
        FwyStateTrajectory simtraj = solver.getFwy().simulate(sim_dt_in_seconds, K_dem, K_cool,sol.get_ramp_flow_in_veh());

        System.out.println("lp: tvm = "+sol.get_tvm() + " tvh = "+ sol.get_tvh());
        System.out.println("sm: tvm = "+simtraj.get_tvm() + " tvh = "+ simtraj.get_tvh());

        sol.print_to_file("out\\x210lpsol", RampMeteringSolution.OutputFormat.matlab);
        simtraj.print_to_file("out\\x210sim");
//        System.out.println(sol.get_lp());

        System.out.println(sol.get_fwy().get_segment(35));

        System.out.println("Feasible: " + solver.getLP().is_feasible(sol,1e-2,true));
        System.out.println("Distance to CTM: " + sol.get_max_ctm_distance());
        System.out.println("Leftover vehicles: " + sol.get_leftover_vehicles());
        System.out.println("Cost: " +  sol.get_cost());
//        System.out.println("J: " + sol.get_lp().get_cost());
        sol.print_ctm_distances();

        System.out.println(sol.get_fwy().as_table(sim_dt_in_seconds));

        assertNotNull(sol);
        //assertTrue(sol.is_ctm());

        System.out.println("done in " + (System.currentTimeMillis()-time));

    }
}
