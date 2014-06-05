package test;

import lp.problem.*;
import org.junit.Before;
import org.junit.Test;
import lp.solver.ApacheSolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gomes on 6/5/14.
 */
public class TestSolver {

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

        problem.add_bound("x", Relation.LEQ, 16);
    }

    @Test
    public void testApache() throws Exception {
        PointValue result = (new ApacheSolver()).solve(problem);
        assertNotNull(result);
        assertEquals(result.get("x"),16,1e-4);
        assertEquals(result.get("y"),59,1e-4);
    }
}
