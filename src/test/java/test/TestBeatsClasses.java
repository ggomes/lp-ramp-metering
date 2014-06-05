package test;

import network.beats.Network;
import jaxb.Scenario;
import org.junit.Test;

public class TestBeatsClasses {

    @Test
    public void testName() throws Exception {
        Scenario scenario = factory.ObjectFactory.getScenario("data/config/_smalltest.xml");
        Network network = (Network) scenario.getNetworkSet().getNetwork().get(0);
        org.junit.Assert.assertEquals(network.getLinkWithId(-1).getBegin_node().getId(),-1);
        org.junit.Assert.assertEquals(network.getNodeWithId(-2).getInput_link()[0].getId(),-1);
    }

}
