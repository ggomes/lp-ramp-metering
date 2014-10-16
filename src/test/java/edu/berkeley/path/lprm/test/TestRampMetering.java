package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by gomes on 10/6/2014.
 */
public class TestRampMetering {

    private double kdem = 450d;
    private double kcool = 180d;
    private double eta = 0.1;
    private double dt = 3;

//    @Test
//    public void testGurobiProblem() throws Exception {
//        System.out.println("Gurobi -----------------------------------");
//        RampMeteringSolver solver = new RampMeteringSolver("data/config/smallNetwork.xml",kdem,kcool,eta,dt);
//        RampMeteringSolution result = solver.solve(SolverType.GUROBI);
//        System.out.println(result.get_cost());
////        System.out.println(result.evaluate_constraint_state(1e-4));
////        solver.print_solver_lp();
////        System.out.println(result);
////        solver.printLP();
//        assertNotNull(result);
//    }

//    @Test
//    public void testApacheProblem() throws Exception {
//        System.out.println("APACHE -----------------------------------");
//        RampMeteringSolver solver = new RampMeteringSolver("data/config/smallNetwork.xml",kdem,kcool,eta,dt);
//        RampMeteringSolution result = solver.solve(SolverType.APACHE);
//        System.out.println(result.get_cost());
////        System.out.println(result.evaluate_constraint_state(1e-4));
////        solver.print_solver_lp();
//        System.out.println(result);
////        result.print_to_file("apache", RampMeteringSolution.OutputFormat.matlab);
////        solver.printLP();
//        assertNotNull(result);
//    }

    @Test
    public void testLpSolveProblem() throws Exception {
        System.out.println("LPSOLVE -----------------------------------");
        RampMeteringSolver solver = new RampMeteringSolver("data/config/smallNetwork.xml",kdem,kcool,eta,dt,false);
        RampMeteringSolution result = solver.solve(SolverType.LPSOLVE);
        System.out.println(result.get_cost());
//        System.out.println(result.evaluate_constraint_state(1e-4));
//        solver.printLP();
//        result.print_to_file("lpsolve", RampMeteringSolution.OutputFormat.matlab);
//        System.out.println(result);
        assertNotNull(result);
    }

}
