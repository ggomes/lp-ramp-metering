package test;

import lp.problem.*;
import lp.solver.ApacheSolver;
import lp.solver.LpSolveSolver;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * max 143 x + 60 y
 * s.t
 *      120 x + 210 y <= 15000
 *      110 x +  30 y <= 4000
 *          x +     y <= 75
 *          x         <= 16
 *
 *  Solution: x=16, y=59
 */
public class TestSolvers {

    private Problem problem;

    @Before
    public void setUp() throws Exception {

        problem = new Problem();

        Linear linear = new Linear();
        linear.add_coefficient(143, "x");
        linear.add_coefficient(60, "y");
        problem.setObjective(linear, OptType.MAX);

        linear = new Linear();
        linear.add_coefficient(120, "x");
        linear.add_coefficient(210, "y");
        linear.set_relation(Relation.LEQ);
        linear.set_rhs(15000);
        problem.add_constraint(linear,"1");

        linear = new Linear();
        linear.add_coefficient(110, "x");
        linear.add_coefficient(30, "y");
        linear.set_relation(Relation.LEQ);
        linear.set_rhs(4000);
        problem.add_constraint(linear,"2");

        linear = new Linear();
        linear.add_coefficient(1, "x");
        linear.add_coefficient(1, "y");
        linear.set_relation(Relation.LEQ);
        linear.set_rhs(75);
        problem.add_constraint(linear, "3");

        problem.add_upper_bound("x", 16);
    }

    @Test
    public void testApache() throws Exception {
        PointValue result = (new ApacheSolver()).solve(problem);
        assertNotNull(result);
        assertEquals(result.get("x"),16,1e-4);
        assertEquals(result.get("y"),59,1e-4);
    }

//    @Test
//    public void testLpSolve() throws Exception {
//        PointValue result = (new LpSolveSolver()).solve(problem);
//        assertNotNull(result);
//        assertEquals(result.get("x"),16,1e-4);
//        assertEquals(result.get("y"),59,1e-4);
//    }

//    @Test
//    public void testGurobi() throws Exception {
//        PointValue result = (new GurobiSolver()).solve(problem);
//        assertNotNull(result);
//        assertEquals(result.get("x"),16,1e-4);
//        assertEquals(result.get("y"),59,1e-4);
//    }
}
