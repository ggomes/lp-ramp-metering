import lp.*;
import solver.ApacheSolver;

/**
 * Created by gomes on 6/4/14.
 */
public class Runner {

    public static void main(String [] args){

        Problem problem = new Problem();

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

        PointValue result = (new ApacheSolver()).solve(problem);

        System.out.println(result);
    }


}
