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
public class BatchRunner3 {

    private static String outfile_prefix = "out/batch_data_10";
    private static String config = "data/config/test_rm_C.xml";
    private static String grid_file = "data/GridPoints3_3.txt";

    private static double or_demand_vps = 0.5d;

    private static boolean print_details = true;
    private static double sim_dt_in_seconds = 1d;
    private static double K_dem_seconds = 400d;
    private static double K_cool_seconds = 200d;
    private static double eta = .1d;

    private static SolverType solver_type = SolverType.GUROBI;
    private static ArrayList<Long> ml_link_ids = new ArrayList<Long>();
    private static BatchWriter batch_data;

    public static void main(String args[]) {

        try {

            // create the lp_solver
            RampMeteringSolver solver = new RampMeteringSolver(config, K_dem_seconds, K_cool_seconds, eta, sim_dt_in_seconds,solver_type,true);

            // populate state link ids
            ml_link_ids.addAll(solver.get_mainline_ids());

            // open output writer
            batch_data = new BatchWriter(outfile_prefix.concat(".txt"));
            ResultPrinterEachRun lp_results_printer = print_details ?  new ResultPrinterEachRun(outfile_prefix) : null;

            // write solver parameters
            solver.write_parameters_to_matlab(outfile_prefix);

            // initial conditions on a grid
            ArrayList<ArrayList<Double>> all_ic = collect_initial_conditions(grid_file, solver.getFwy());

            // gurobi needs it solved initially for rhs override to work.
            RampMeteringSolution x = solver.solve();

            int index = 0;
            for (ArrayList<Double> ic : all_ic) {
                index += 1;
                System.out.println(index + " of " + all_ic.size());

                // check
                if(ml_link_ids.size()!=ic.size())
                    throw new Exception("ic not same size as ml_ids");

                // set initial condition
                for (int i = 0; i < ml_link_ids.size(); i++)
                    solver.set_density_in_veh(ml_link_ids.get(i), ic.get(i));

                // run solver
                RampMeteringSolution sol = solver.solve();

                // check ctm-ness
                Double CTMcheck = sol.get_max_ctm_distance() == null ? -1d : sol.get_max_ctm_distance();

                // write output
                batch_data.write(batch_data.getTableString(index, ic, or_demand_vps, CTMcheck, sol.get_tvh(), sol.get_tvm(),sol.get_cost()));
                if(print_details)
                    lp_results_printer.print_lp_results(sol, sol.K, index);
            }

            if(print_details)
                lp_results_printer.print_config_data(solver.getFwy());

        } catch (Exception e) {
            System.err.print(e);
        } finally {
            batch_data.close();
        }


    }

    private static ArrayList<ArrayList<Double>> collect_initial_conditions(String grid_file_address,FwyNetwork fwy) throws Exception {

        // create the list of initial densities to test
        ArrayList<ArrayList<Double>> all_ic = new ArrayList<ArrayList<Double>>();

        try {

            // read grid points
            ReadFile file = new ReadFile(grid_file_address);
            ArrayList<ArrayList<Double>> gridValues = file.getTextContents();
            for (ArrayList<Double> grid : gridValues)
                for (Double densityFraction :grid)
                    if (densityFraction > 1)
                        throw new Exception("Density fraction greater than 1\n");

            int i0,i1,i2;
            ArrayList<Double> grid0 = gridValues.get(0);
            ArrayList<Double> grid1 = gridValues.get(1);
            ArrayList<Double> grid2 = gridValues.get(2);
            double jam_density0 = fwy.get_njam_veh_per_link(ml_link_ids.get(0));
            double jam_density1 = fwy.get_njam_veh_per_link(ml_link_ids.get(1));
            double jam_density2 = fwy.get_njam_veh_per_link(ml_link_ids.get(2));

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
