package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.lp.problem.*;
import edu.berkeley.path.lprm.lp.solver.ApacheSolver;
import edu.berkeley.path.lprm.lp.solver.LpSolveSolver;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by gomes on 10/6/2014.
 */
public class TestRampMetering {

    @Test
    public void testApacheProblem() throws Exception {
        System.out.println("APACHE -----------------------------------");
        RampMeteringSolver solver = new RampMeteringSolver("data/config/smallNetwork.xml",12d,12d,.1d,3d);
        RampMeteringSolution result = solver.solve(SolverType.APACHE);
        solver.print_solver_lp();
//        System.out.println(result);
        assertNotNull(result);
    }

    @Test
    public void testLpSolveProblem() throws Exception {
        System.out.println("LPSOLVE -----------------------------------");
        RampMeteringSolver solver = new RampMeteringSolver("data/config/smallNetwork.xml",12d,12d,.1d,3d);
        RampMeteringSolution result = solver.solve(SolverType.LPSOLVE);
        solver.printLP();
//        System.out.println(result);
        assertNotNull(result);
    }

}
