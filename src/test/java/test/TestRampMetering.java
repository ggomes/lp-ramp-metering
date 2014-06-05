package test;

import jaxb.*;
import org.junit.Before;
import org.junit.Test;
import ramp_metering.RampMeteringLimitSet;
import ramp_metering.RampMeteringLpPolicyMaker;
import ramp_metering.RampMeteringPolicyMaker;
import ramp_metering.RampMeteringPolicySet;

/**
 * Created by gomes on 6/5/14.
 */
public class TestRampMetering {

    private Scenario scenario;

    @Before
    public void setUp() throws Exception {
        scenario = entry.ObjectFactory.getScenario("data/config/210W_v13.xml");
    }

    @Test
    public void testRampMetering() throws Exception {

        RampMeteringLpPolicyMaker rmpm = new RampMeteringLpPolicyMaker();

        Network net = scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fd = scenario.getFundamentalDiagramSet();
        DemandSet demand = scenario.getDemandSet();
        SplitRatioSet splitRatios = scenario.getSplitRatioSet();
        InitialDensitySet ics = scenario.getInitialDensitySet();
        RampMeteringLimitSet limit_set = null;
        Double dt = 5d;

        RampMeteringPolicySet policy_set = rmpm.givePolicy(net,fd,demand,splitRatios,ics,limit_set,dt);

        System.out.println(policy_set);
    }
}
