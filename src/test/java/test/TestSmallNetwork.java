package test;

import jaxb.*;
import lp.RampMeteringLpPolicyMaker;
import lp.RampMeteringSolution;
import lp.solver.SolverType;
import network.beats.Network;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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


    private SolverType solver_type = SolverType.LPSOLVE;

    @Before
    public void setUp() throws Exception {
        String config = "data/config/smallNetwork.xml";
        scenario = factory.ObjectFactory.getScenario(config);
    }

    @Test
    public void testSmallNetwork() throws Exception {

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        Network net = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        RampMeteringLpPolicyMaker policy_maker = new RampMeteringLpPolicyMaker(net, fds, split_ratios, actuators, K_dem, K_cool, eta, sim_dt_in_seconds);

        ArrayList<String> errors = policy_maker.getFwy().check_CFL_condition(sim_dt_in_seconds);
        if (!errors.isEmpty()) {
            System.err.print(errors);
            throw new Exception("CFL error");
        }
        InitialDensitySet ics = scenario.getInitialDensitySet();
        DemandSet demands = scenario.getDemandSet();
        policy_maker.set_data(ics, demands);
        RampMeteringSolution sol = policy_maker.solve(solver_type);

        //policy_maker.printLP();


        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.matlab);
        sol.print_to_file("SmallNetwork", RampMeteringSolution.OutputFormat.text);
        System.out.println(sol);

        assertNotNull(sol);
        boolean CTMbehavior = true;
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < (K_cool + K_dem); j++) {
                if (sol.is_flow_CTM_behavior(policy_maker.getFwy())[i][j] == false) {
                CTMbehavior = false;
                break;}
            }
        }
        assertEquals(true, CTMbehavior);




    }
}


