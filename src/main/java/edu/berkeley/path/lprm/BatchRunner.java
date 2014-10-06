package edu.berkeley.path.lprm;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;

import java.util.ArrayList;

/**
 * Created by gomes on 10/6/14.
 */
public class BatchRunner {

//    private static Scenario scenario;
//    private static double sim_dt_in_seconds = 3d;
//    private static double K_dem_seconds = 9d;
//    private static double K_cool_seconds = 15d;
//    private static double eta = .1d;
//    private static int num_links;
//    private static int num_segments;
//    //    private double[] initialDensityVector;
//    private static ArrayList<Long> link_ids;
//    private SolverType solver_type = SolverType.LPSOLVE;

    public static void main(String args[]) throws Exception {

        Scenario scenario;
        double sim_dt_in_seconds = 3d;
        double K_dem_seconds = 9d;
        double K_cool_seconds = 15d;
        double eta = .1d;
        int num_links;
        int num_segments;
        ArrayList<Long> link_ids;
        SolverType solver_type = SolverType.LPSOLVE;

        String config = "data/config/smallNetwork.xml";
        scenario = ObjectFactory.getScenario(config);

        int K_dem = (int) Math.round(K_dem_seconds / sim_dt_in_seconds);
        int K_cool = (int) Math.round(K_cool_seconds / sim_dt_in_seconds);

        Network net = scenario.getNetworkSet().getNetwork().get(0);
        FundamentalDiagramSet fds = scenario.getFundamentalDiagramSet();
        ActuatorSet actuators = scenario.getActuatorSet();
        SplitRatioSet split_ratios = scenario.getSplitRatioSet();
        RampMeteringSolver policy_maker = new RampMeteringSolver(net, fds, split_ratios, actuators, K_dem, K_cool, eta, sim_dt_in_seconds);

        ArrayList<String> errors = policy_maker.getFwy().check_cfl_condition(sim_dt_in_seconds);
        if (!errors.isEmpty()) {
            System.err.print(errors);
            throw new Exception("CFL error");
        }

        link_ids = policy_maker.getFwy().get_link_ids();
        num_links = link_ids.size();

        int num_states = policy_maker.getFwy().get_num_states();

        int nDivisions = 1;
        double max_demand = 20;

        double init_dens_row[] = new double[num_states];
        String row_to_print;

        double densityValue;
        Double jamDensity;
//        String file_address = "C:/batch_table.txt";
        String file_address = "C:/Documents and Settings/negar/code/L0/lp-ramp-metering/out/batch_table";

        int index = 0;

        BatchWriter batch_data = new BatchWriter(file_address.concat(".txt"),true);
        ResultPrinterEachRun lp_results_printer = new ResultPrinterEachRun(file_address);


        // Some way of producing different permutations of initial densities should be found

        for (int i = 0; i < num_states; i++) {

            jamDensity = policy_maker.getFwy().get_link_jam_density(link_ids.get(i),fds,net);

            for (int j=0;j<=nDivisions;j++){
                index += 1;
                densityValue = ++j *(jamDensity/nDivisions);
                init_dens_row[i] = densityValue;
                Double demandValue = 1.5;

                policy_maker.set_density_link_network(link_ids.get(i),densityValue);
                policy_maker.set_demand_link_network(link_ids.get(i),demandValue,sim_dt_in_seconds);
                policy_maker.set_rhs_link(link_ids.get(i),demandValue);
                RampMeteringSolution sol = policy_maker.solve(solver_type);

                if (i==0 && j==1){
                    lp_results_printer.print_config_data(sol);
                }
                batch_data.writeToFile(batch_data.getTableString(index,init_dens_row,demandValue,sol.is_CTM(),sol.getTVH(),sol.getTVM()));
                lp_results_printer.print_lp_results(sol,sol.K,index);
            }
        }

    }
}
