package test;

import jaxb.*;
import lp.RampMeteringLpPolicyMaker;
import lp.RampMeteringSolution;
import lp.solver.SolverType;
import network.beats.Network;
import org.junit.*;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

public class TestSmallNetwork {

    private Scenario scenario;

    @Before
    public void setUp() throws Exception {
        String config = "data/config/smallNetwork.xml";
        scenario = factory.ObjectFactory.getScenario(config);
    }

    @Test
    public void testSmallNetwork() throws Exception {

        double sim_dt_in_seconds = 3;
        int K_dem = (int) Math.round(12/sim_dt_in_seconds);
        int K_cool = (int) Math.round(6/sim_dt_in_seconds);
        double eta = .1d;

        Network net = (Network) scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        RampMeteringLpPolicyMaker policy_maker = new RampMeteringLpPolicyMaker(net,fds,split_ratios,actuators,K_dem,K_cool,eta,sim_dt_in_seconds);

        ArrayList<String> errors = policy_maker.getFwy().check_CFL_condition(sim_dt_in_seconds);
        if(!errors.isEmpty()){
            System.err.print(errors);
            throw new Exception("CFL error");
        }

        InitialDensitySet ics = scenario.getInitialDensitySet();
        DemandSet demands = scenario.getDemandSet();
        policy_maker.set_data(ics,demands);

        policy_maker.printLP();

        RampMeteringSolution sol = policy_maker.solve(SolverType.LPSOLVE);

        System.out.println("\n\nSOLVED:\n"+sol.print(true));

        assertNotNull(sol);

    }

}
