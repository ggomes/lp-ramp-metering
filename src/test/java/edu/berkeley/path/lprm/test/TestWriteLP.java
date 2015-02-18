package edu.berkeley.path.lprm.test;

import edu.berkeley.path.beats.jaxb.Density;
import edu.berkeley.path.beats.jaxb.InitialDensitySet;
import edu.berkeley.path.beats.jaxb.Link;
import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.lprm.ObjectFactory;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by gomes on 2/18/2015.
 */
public class TestWriteLP {

    @Test
    public void test_write_small() throws Exception {

        double K_dem_seconds = 3600;
        double K_cool_seconds = 600;
        double sim_dt_in_seconds = 5;
        double eta = 0.1d;
        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);
        String config = "data\\smallNetwork2seg.xml";

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
        RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem, K_cool, eta, sim_dt_in_seconds,null,true);

        solver.getLP().write_to_files("out//");

        assertTrue(true);
    }

}
