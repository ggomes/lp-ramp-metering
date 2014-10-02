package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.jaxb.Scenario;
import edu.berkeley.path.lprm.factory.ObjectFactory;
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
        scenario = ObjectFactory.getScenario("data/config/smallNetwork.xml");
        assertNotNull(scenario);
        assertEquals(scenario.getId(),-110);
    }
}
