package edu.berkeley.path.lprm;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
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
        SolverType solver_type = SolverType.LPSOLVE;

        String config = "data/config/smallNetwork.xml";

        // create the solver
        RampMeteringSolver solver = new RampMeteringSolver(config,K_dem_seconds,K_cool_seconds,eta,sim_dt_in_seconds);

        // check for cfl errors
        ArrayList<String> errors = solver.check_cfl_condition();
        if (!errors.isEmpty()) {
            System.err.print(errors);
            throw new Exception("CFL error");
        }

        // get all state link ids
        ArrayList<Long> link_ids = new ArrayList<Long>();
        link_ids.addAll(solver.get_mainline_ids());
        link_ids.addAll(solver.get_metered_onramp_ids());

        int num_states = link_ids.size();

        // create the list of initial densities to test
        ArrayList<ArrayList<Double>> all_ic = new ArrayList<ArrayList<Double>>();

        // insert an algorithm for generating permutations here...
        ArrayList<Double> one_ic = new ArrayList<Double>();
        for(int i=0;i<link_ids.size();i++) {
            double jam_density = solver.getFwy().get_link_jam_density(link_ids.get(i));
            double value = 0.5d;
            one_ic.add(jam_density*value);
        }
        all_ic.add(one_ic);

        // iterate through ic
        String file_address = "C:/Documents and Settings/negar/code/L0/lp-ramp-metering/out/batch_table";
        BatchWriter batch_data = new BatchWriter(file_address.concat(".txt"),true);
        ResultPrinterEachRun lp_results_printer = new ResultPrinterEachRun(file_address);

        for( ArrayList<Double> ic : all_ic){
            for(int i=0;i<link_ids.size();i++) {
                long link_id = link_ids.get(i);
                solver.set_density(link_id, ic.get(i));
//                solver.set_demand_link_network(link_id, demandValue, sim_dt_in_seconds);
//                solver.set_rhs_link(link_ids.get(i), demandValue);
            }
            solver.read_rhs_from_fwy();
            RampMeteringSolution sol = solver.solve(solver_type);

//            if (i==0 && j==1){
//                lp_results_printer.print_config_data(sol);
//            }
//            batch_data.writeToFile(batch_data.getTableString(index,init_dens_row,demandValue,sol.is_ctm(),sol.getTVH(),sol.getTVM()));
//            lp_results_printer.print_lp_results(sol,sol.K,index);
        }


        /////////////////////////////////////////////////////////////////

//        // iterate through segments in the freeway, add a state for each mainline or metered onramp
//
//        // (NOTE: the solver need not expose fwy)
//        link_ids = solver.getFwy().get_link_ids();
//        num_links = link_ids.size();
//
//        int num_states = solver.getFwy().get_num_states();
//
//        int nDivisions = 1;
//        double max_demand = 20;
//
//        double init_dens_row[] = new double[num_states];
//        String row_to_print;
//
//        double densityValue;
//        Double jamDensity;
////        String file_address = "C:/batch_table.txt";
//        String file_address = "C:/Documents and Settings/negar/code/L0/lp-ramp-metering/out/batch_table";
//
//        int index = 0;
//
//        BatchWriter batch_data = new BatchWriter(file_address.concat(".txt"),true);
//        ResultPrinterEachRun lp_results_printer = new ResultPrinterEachRun(file_address);
//
//        // Some way of producing different permutations of initial densities should be found
//
//        for (int i = 0; i < num_states; i++) {
//
//            // (NOTE: Why should one have to provide fds and net?)
//            jamDensity = solver.getFwy().get_link_jam_density(link_ids.get(i),fds,net);
//
//            for (int j=0;j<=nDivisions;j++){
//                index += 1;
//                densityValue = ++j *(jamDensity/nDivisions);
//                init_dens_row[i] = densityValue;
//                Double demandValue = 1.5;
//
//                solver.set_density(link_ids.get(i), densityValue);
//                solver.set_demand_link_network(link_ids.get(i), demandValue, sim_dt_in_seconds);
//                solver.set_rhs_link(link_ids.get(i), demandValue);
//                RampMeteringSolution sol = solver.solve(solver_type);
//
//                if (i==0 && j==1){
//                    lp_results_printer.print_config_data(sol);
//                }
//                batch_data.writeToFile(batch_data.getTableString(index,init_dens_row,demandValue,sol.is_ctm(),sol.getTVH(),sol.getTVM()));
//                lp_results_printer.print_lp_results(sol,sol.K,index);
//            }
//        }

    }
}
