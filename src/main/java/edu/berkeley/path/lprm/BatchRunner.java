package edu.berkeley.path.lprm;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;

import java.util.ArrayList;

/**
 * Created by gomes on 10/6/14.
 */
public class BatchRunner {


    public static void main(String args[]) throws Exception {

        Scenario scenario;
        double sim_dt_in_seconds = 3d;
        double K_dem_seconds = 36d;
        double K_cool_seconds = 12d;
        double eta = .1d;
        int num_links;
        int num_segments;
        SolverType solver_type = SolverType.GUROBI;

        String config = "data/config/smallNetworkModified.xml";

        // create the lp_solver
        RampMeteringSolver solver = new RampMeteringSolver(config, K_dem_seconds, K_cool_seconds, eta, sim_dt_in_seconds,solver_type,true);

        // get all state link ids
        ArrayList<Long> link_ids = new ArrayList<Long>();
        ArrayList<Long> state_link_ids = new ArrayList<Long>();
        link_ids.addAll(solver.get_mainline_ids());
        link_ids.addAll(solver.get_metered_onramp_ids());
        state_link_ids.addAll(solver.get_mainline_ids());

        int num_states = link_ids.size();
        double CTM_distance;
        double epsilon = 1e-2;
        int nDivisions = 2;

        // create the list of initial densities to test
        ArrayList<ArrayList<Double>> all_ic = new ArrayList<ArrayList<Double>>();


        String grid_file_address = "data/GridPoints.txt";
        ReadFile file = new ReadFile(grid_file_address);
        ArrayList<ArrayList<Double>> gridValues = file.getTextContents();

        for (ArrayList<Double> grid : gridValues){
            for (Double densityFraction :grid){
                if (densityFraction > 1)
                    throw new Exception("Density fraction greater than 1\n");
            }
        }


            // insert an algorithm for generating permutations here...
        // Some nice way of producing permutations should be added here

//        for (int i0=0;i0<=nDivisions;i0++) {
//           double jam_density0 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(0));
//           for (int i1=0; i1<=nDivisions;i1++){
//               double jam_density1 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(1));
//               for (int i2=0;i2<=nDivisions;i2++){
//                   double jam_density2 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(2));
//                     ArrayList<Double> one_ic = new ArrayList<Double>();
//                     one_ic.add(0,(jam_density0/nDivisions)*i0);
//                     one_ic.add(1,(jam_density1/nDivisions)*i1);
//                     one_ic.add(2,(jam_density2/nDivisions)*i2);
//                     all_ic.add(one_ic);
//               }
//
//           }
//       }



//        for (int i0=0; i0<gridValues.get(0).size(); i0++) {
//            double jam_density0 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(0));
//            for (int i1=0; i1<gridValues.get(1).size(); i1++){
//                double jam_density1 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(1));
//                for (int i2=0; i2<gridValues.get(2).size(); i2++){
//                    double jam_density2 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(2));
//                    ArrayList<Double> one_ic = new ArrayList<Double>();
//                    one_ic.add(0,jam_density0*gridValues.get(0).get(i0));
//                    one_ic.add(1,jam_density1*gridValues.get(1).get(i1));
//                    one_ic.add(2,jam_density2*gridValues.get(2).get(i2));
//                    all_ic.add(one_ic);
//                }
//            }
//        }

        for (int i0=0; i0<gridValues.get(0).size(); i0++) {
            double jam_density0 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(0));
            for (int i1=0; i1<gridValues.get(1).size(); i1++){
                double jam_density1 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(1));
                for (int i2=0; i2<gridValues.get(2).size(); i2++){
                    double jam_density2 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(2));
                    ArrayList<Double> one_ic = new ArrayList<Double>();
                    one_ic.add(0,gridValues.get(0).get(i0));
                    one_ic.add(1,gridValues.get(1).get(i1));
                    one_ic.add(2,gridValues.get(2).get(i2));
                    all_ic.add(one_ic);
                }
            }
        }



        // iterate through ic
//        String file_address = "C:/Documents and Settings/negar/code/L0/lp-ramp-metering/out/batch_table";
        //String file_address = "C:/Documents and Settings/negar/code/L0/lp-ramp-metering/out/batch_data";
        String file_address = "out/batch_data_modified";
        BatchWriter batch_data = new BatchWriter(file_address.concat(".txt"), true);
        ResultPrinterEachRun lp_results_printer = new ResultPrinterEachRun(file_address);


//        ArrayList<Double> one_ic = new ArrayList<Double>();
//        one_ic.add(0,0d);
//        double njam2 = solver.getFwy().get_njam_veh_per_link(state_link_ids.get(2));
//        one_ic.add(1,0d);
//        one_ic.add(2,njam2/2);
//
//
//        double demand_value = 2d;
//            for(int i=0;i<state_link_ids.size();i++) {
//                long link_id = state_link_ids.get(i);
//                solver.set_density_in_veh(link_id, one_ic.get(i));
//
//                solver.set_demand_in_vps(link_id, demand_value, sim_dt_in_seconds);
//
//            }
//
//            solver.read_rhs_from_fwy();
//            RampMeteringSolution sol = solver.solve(solver_type);
//            System.out.println("CTM distance = " + sol.get_max_ctm_distance()  +"\tCost = " + sol.get_cost() + "\t" + one_ic);
//            System.out.println(sol);


//        ArrayList<ArrayList<Double>> bandwidth_array = new ArrayList<ArrayList<Double>>();

        RampMeteringSolution x = solver.solve();
        int index = 0;
        for (ArrayList<Double> ic : all_ic) {
            index += 1;
            double demand_value = 0.5d;
            for (int i = 0; i < state_link_ids.size(); i++) {
                long link_id = state_link_ids.get(i);
                System.out.println(ic+"\t"+i);
                solver.set_density_in_veh(link_id, ic.get(i));
                solver.set_demand_in_vps(link_id, demand_value);
            }

            RampMeteringSolution sol = solver.solve();

//            System.out.println(sol);
            Double CTMcheck;
            CTMcheck = sol.get_max_ctm_distance() == null ? -1d : sol.get_max_ctm_distance();
            batch_data.writeToFile(batch_data.getTableString(index, ic, demand_value, CTMcheck, sol.get_tvh(), sol.get_tvm(),sol.get_cost()));
            lp_results_printer.print_lp_results(sol, sol.K, index);
        }
        lp_results_printer.print_config_data(solver.getFwy());




        /////////////////////////////////////////////////////////////////

//        // iterate through segments in the freeway, add a state for each mainline or metered onramp
//
//        // (NOTE: the lp_solver need not expose fwy)
//        link_ids = lp_solver.getFwy().get_link_ids();
//        num_links = link_ids.size();
//
//        int num_states = lp_solver.getFwy().get_num_states();
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
//            jamDensity = lp_solver.getFwy().get_njam_veh_per_link(link_ids.get(i),fds,net);
//
//            for (int j=0;j<=nDivisions;j++){
//                index += 1;
//                densityValue = ++j *(jamDensity/nDivisions);
//                init_dens_row[i] = densityValue;
//                Double demandValue = 1.5;
//
//                lp_solver.set_density_in_vpm(link_ids.get(i), densityValue);
//                lp_solver.set_demand_link_network(link_ids.get(i), demandValue, sim_dt_in_seconds);
//                lp_solver.set_rhs_link(link_ids.get(i), demandValue);
//                RampMeteringSolution sol = lp_solver.solve(solver_type);
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
