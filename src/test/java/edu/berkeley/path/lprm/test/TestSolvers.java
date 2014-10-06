package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.lp.problem.*;
import edu.berkeley.path.lprm.lp.solver.ApacheSolver;
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

    private Problem problem_A;
    private Problem problem_B;

    @Before
    public void setUp() throws Exception {
        problem_A = load_problem_A();
        problem_B = load_problem_B();
    }

    @Test
    public void testApacheProblemA() throws Exception {
        PointValue result = (new ApacheSolver()).solve(problem_A);
        assertNotNull(result);
        assertEquals(result.get("x"),16,1e-4);
        assertEquals(result.get("y"),59,1e-4);
    }

    @Test
    public void testApacheProblemB() throws Exception {
        PointValue result = (new ApacheSolver()).solve(problem_B);
        assertNotNull(result);
    }

//    @Test
//    public void testLpSolve() throws Exception {
//        PointValue result = (new LpSolveSolver()).solve(problem_A);
//        assertNotNull(result);
//        assertEquals(result.get("x"),16,1e-4);
//        assertEquals(result.get("y"),59,1e-4);
//    }

//    @Test
//    public void testGurobi() throws Exception {
//        PointValue result = (new GurobiSolver()).solve(problem_A);
//        assertNotNull(result);
//        assertEquals(result.get("x"),16,1e-4);
//        assertEquals(result.get("y"),59,1e-4);
//    }

    private Problem load_problem_A(){

        Problem P = new Problem();

        Linear linear = new Linear();
        linear.add_coefficient(143, "x");
        linear.add_coefficient(60, "y");
        P.setObjective(linear, OptType.MAX);

        linear = new Linear();
        linear.add_coefficient(120, "x");
        linear.add_coefficient(210, "y");
        linear.set_relation(Relation.LEQ);
        linear.set_rhs(15000);
        P.add_constraint(linear, "1");

        linear = new Linear();
        linear.add_coefficient(110, "x");
        linear.add_coefficient(30, "y");
        linear.set_relation(Relation.LEQ);
        linear.set_rhs(4000);
        P.add_constraint(linear, "2");

        linear = new Linear();
        linear.add_coefficient(1, "x");
        linear.add_coefficient(1, "y");
        linear.set_relation(Relation.LEQ);
        linear.set_rhs(75);
        P.add_constraint(linear, "3");

        P.add_upper_bound("x", 16);
        return P;
    }

    private Problem load_problem_B(){
        return null;
    }

}
