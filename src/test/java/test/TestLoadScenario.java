package test;

import jaxb.Scenario;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gomes on 6/5/14.
 */
public class TestLoadScenario {
    private Scenario scenario;
    @Test
    public void testName() throws Exception {
        scenario = factory.ObjectFactory.getScenario("data/config/smallNetwork.xml");
        assertNotNull(scenario);
        assertEquals(scenario.getId(),-110);
    }
}
