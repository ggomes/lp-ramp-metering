package edu.berkeley.path.lprm.test;

import edu.berkeley.path.lprm.lp.problem.*;
import edu.berkeley.path.lprm.lp.solver.ApacheSolver;
import edu.berkeley.path.lprm.lp.solver.LpSolveSolver;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;
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
        System.out.println("APACHE -----------------------------------");
        System.out.println(result.get_cost());
        System.out.println(result);
        assertNotNull(result);
    }

    @Test
    public void testLpSolveProblemA() throws Exception {
        PointValue result = (new LpSolveSolver()).solve(problem_A);
        assertNotNull(result);
        assertEquals(result.get("x"),16,1e-4);
        assertEquals(result.get("y"),59,1e-4);
    }

    @Test
    public void testLpSolveProblemB() throws Exception {
        PointValue result = (new LpSolveSolver()).solve(problem_B);
        System.out.println("LPSOLVE -----------------------------------");
        assertNotNull(result);
        System.out.println(result.get_cost());
//        assertEquals(4.112537005198651,result.get_cost(),1e-6);
    }

    private Problem load_problem_A(){

        Problem P = new Problem();

        Linear linear = new Linear();
        linear.add_coefficient(143, "x");
        linear.add_coefficient(60, "y");
        P.setObjective(linear, OptType.MAX);

        Constraint cnst = new Constraint();
        cnst.add_coefficient(120, "x");
        cnst.add_coefficient(210, "y");
        cnst.set_relation(Relation.LEQ);
        cnst.set_rhs(15000);
        P.add_constraint(cnst, "1");

        cnst = new Constraint();
        cnst.add_coefficient(110, "x");
        cnst.add_coefficient(30, "y");
        cnst.set_relation(Relation.LEQ);
        cnst.set_rhs(4000);
        P.add_constraint(cnst, "2");

        cnst = new Constraint();
        cnst.add_coefficient(1, "x");
        cnst.add_coefficient(1, "y");
        cnst.set_relation(Relation.LEQ);
        cnst.set_rhs(75);
        P.add_constraint(cnst, "3");

        P.add_upper_bound("x", 16);
        return P;
    }

    private Problem load_problem_B(){
        try {
            RampMeteringSolver solver = new RampMeteringSolver("data/config/smallNetwork.xml",12d,12d,.1d,3d,false);
            return solver.getLP();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
