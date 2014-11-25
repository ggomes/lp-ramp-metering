package edu.berkeley.path.lprm;

import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gomes on 10/6/14.
 */
public class BatchRunner {

    private static String file_address = "out/batch_data_10";
    private static String config = "data/config/test_rm_C.xml";
    private static String grid_file = "data/GridPoints_10.txt";
    private static double sim_dt_in_seconds = 3d;
    private static double K_dem_seconds = 60d;
    private static double K_cool_seconds = 60d;
    private static double eta = .1d;
    private static SolverType solver_type = SolverType.GUROBI;
    private static ArrayList<Long> state_link_ids = new ArrayList<Long>();
    private static boolean print_details = true;
    private static double demand_value = 0.5d;

    public static void main(String args[]) throws Exception {

        // create the lp_solver
        RampMeteringSolver solver = new RampMeteringSolver(config, K_dem_seconds, K_cool_seconds, eta, sim_dt_in_seconds,solver_type,true);

        // populate state link ids
        state_link_ids.addAll(solver.get_mainline_ids());

        // initial conditions on a grid
        ArrayList<ArrayList<Double>> all_ic = collect_initial_conditions(grid_file,solver.getFwy());

        // open output writer
        BatchWriter batch_data = new BatchWriter(file_address.concat(".txt"), true);
        ResultPrinterEachRun lp_results_printer = print_details ?  new ResultPrinterEachRun(file_address) : null;

        // gurobi needs it solved initially for rhs override to work.
        RampMeteringSolution x = solver.solve();

        int index = 0;
        for (ArrayList<Double> ic : all_ic) {
            index += 1;
            System.out.println(index + " of " + all_ic.size());

            for (int i = 0; i < state_link_ids.size(); i++) {
                long link_id = state_link_ids.get(i);
                solver.set_density_in_veh(link_id, ic.get(i));
                solver.set_demand_in_vps(link_id, demand_value);
            }

            RampMeteringSolution sol = solver.solve();
            Double CTMcheck = sol.get_max_ctm_distance() == null ? -1d : sol.get_max_ctm_distance();
            batch_data.writeToFile(batch_data.getTableString(index, ic, demand_value, CTMcheck, sol.get_tvh(), sol.get_tvm(),sol.get_cost()));
            if(print_details)
                lp_results_printer.print_lp_results(sol, sol.K, index);
        }

        if(print_details)
            lp_results_printer.print_config_data(solver.getFwy());

    }


    private static ArrayList<ArrayList<Double>> collect_initial_conditions(String grid_file_address,FwyNetwork fwy) throws Exception {

        // create the list of initial densities to test
        ArrayList<ArrayList<Double>> all_ic = new ArrayList<ArrayList<Double>>();

        try {

            // read grid points
            ReadFile file = new ReadFile(grid_file_address);
            ArrayList<ArrayList<Double>> gridValues = file.getTextContents();
            for (ArrayList<Double> grid : gridValues){
                for (Double densityFraction :grid){
                    if (densityFraction > 1)
                        throw new Exception("Density fraction greater than 1\n");
                }
            }


            int i0,i1,i2;
            ArrayList<Double> grid0 = gridValues.get(0);
            ArrayList<Double> grid1 = gridValues.get(1);
            ArrayList<Double> grid2 = gridValues.get(2);
            double jam_density0 = fwy.get_njam_veh_per_link(state_link_ids.get(0));
            double jam_density1 = fwy.get_njam_veh_per_link(state_link_ids.get(1));
            double jam_density2 = fwy.get_njam_veh_per_link(state_link_ids.get(2));

            for (i0=0; i0<grid0.size(); i0++)
                for (i1=0; i1<grid1.size(); i1++)
                    for (i2=0; i2<grid2.size(); i2++){
                        ArrayList<Double> one_ic = new ArrayList<Double>();
                        one_ic.add( grid0.get(i0) * jam_density0 );
                        one_ic.add( grid1.get(i1) * jam_density1 );
                        one_ic.add( grid2.get(i2) * jam_density2 );
                        all_ic.add(one_ic);
                    }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return all_ic;

    }







}
