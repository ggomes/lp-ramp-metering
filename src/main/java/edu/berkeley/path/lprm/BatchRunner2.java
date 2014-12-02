package edu.berkeley.path.lprm;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;
import edu.berkeley.path.lprm.rm.RampMeteringSolver;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by gomes on 11/25/14.
 */
public class BatchRunner2 {

    private static String grid_file = "data/GridPoints2_10.txt";
    private static boolean print_details = true;
    private static double sim_dt_in_seconds = 1d;
    private static double eta = .1d;
    private static SolverType solver_type = SolverType.GUROBI;

    private static String config = "data/config/test_rm_D.xml";
    private static double K_dem_seconds = 400d;
    private static double K_cool_seconds = 200d;

    public static void main(String args[]) {

        Double [] or_demand_values = {0.1,0.3,0.5};         // 3
        Double [] split_values = {0d,0.2,0.8};              // 3
        Double [] w_values = {0.1,0.5,0.9};                 // 3
        Double [] max_metering_values = {360d,1080d,1800d};    // 3

        // generate all run_params
        ArrayList<RunParam> run_param = new ArrayList<RunParam>();
        int c = 0;
        for(Double or_demand : or_demand_values)
            for(Double split : split_values)
                for(Double w : w_values)
                    for(Double max_metering : max_metering_values)
                        run_param.add(new RunParam(String.format("out//b%d",c++),or_demand,split,w,max_metering));

        // run batch
        try {
            PrintWriter batch_info = new PrintWriter(new FileWriter("out//batch_info.txt"));
            for(RunParam rp : run_param) {
                batch_info.write(String.format("%f\t%f\t%f\t%f\n",rp.or_demand_vps,rp.split,rp.w,rp.max_metering));
                System.out.print(rp);
                run_batch(rp);
            }
            batch_info.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void run_batch(RunParam run_param){

        String outfile_prefix = run_param.outfile_prefix;
        double or_demand_vps = run_param.or_demand_vps;
        double split = run_param.split;
        double w = run_param.w;
        double max_metering = run_param.max_metering;

        BatchWriter batch_data = null;
        ArrayList<Long> ml_link_ids = new ArrayList<Long>();

        try {

            // load scenario
            Scenario scenario = ObjectFactory.getScenario(config);

            // set split
            for(SplitRatioProfile srp : scenario.getSplitRatioSet().getSplitRatioProfile())
                for(Splitratio sr : srp.getSplitratio())
                    sr.setContent(String.format("%f",split));

            // set onramp demand
            for(DemandProfile dp : scenario.getDemandSet().getDemandProfile())
                for(Demand dem : dp.getDemand())
                    dem.setContent(String.format("%f",or_demand_vps));

            // set congestion speed
            for(FundamentalDiagramProfile fdp : scenario.getFundamentalDiagramSet().getFundamentalDiagramProfile())
                for(FundamentalDiagram fd: fdp.getFundamentalDiagram())
                    fd.setCongestionSpeed(w);

            // set maximum metering rate
            for(Actuator act : scenario.getActuatorSet().getActuator())
                for(Parameter par : act.getParameters().getParameter())
                    if(par.getName().equalsIgnoreCase("max_rate_in_vphpl"))
                        par.setValue(String.format("%f",max_metering));

            // create the lp_solver
            RampMeteringSolver solver = new RampMeteringSolver(scenario, K_dem_seconds, K_cool_seconds, eta, sim_dt_in_seconds,solver_type,true);

            // populate state link ids
            ml_link_ids.addAll(solver.get_mainline_ids());

            // open output writer
            batch_data = new BatchWriter(outfile_prefix.concat(".txt"));
            ResultPrinterEachRun lp_results_printer = print_details ?  new ResultPrinterEachRun(outfile_prefix) : null;

            // write solver parameters
            solver.write_parameters_to_matlab(outfile_prefix);

            // initial conditions on a grid
            ArrayList<ArrayList<Double>> all_ic = collect_initial_conditions(grid_file, solver.getFwy(),ml_link_ids);

            // gurobi needs it solved initially for rhs override to work.
            RampMeteringSolution x = solver.solve();

            int index = 0;
            for (ArrayList<Double> ic : all_ic) {
                index += 1;
//                System.out.println(index + " of " + all_ic.size());

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

//            if(print_details)
//                lp_results_printer.print_config_data(solver.getFwy());

        } catch (Exception e) {
            System.err.print(e);
        } finally {
            batch_data.close();
        }

    }

    private static ArrayList<ArrayList<Double>> collect_initial_conditions(String grid_file_address,FwyNetwork fwy,ArrayList<Long> ml_link_ids) throws Exception {

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
            double jam_density0 = fwy.get_njam_veh_per_link(ml_link_ids.get(0));
            double jam_density1 = fwy.get_njam_veh_per_link(ml_link_ids.get(1));

            for (i0=0; i0<grid0.size(); i0++)
                for (i1=0; i1<grid1.size(); i1++) {
                    ArrayList<Double> one_ic = new ArrayList<Double>();
                    one_ic.add( grid0.get(i0) * jam_density0 );
                    one_ic.add( grid1.get(i1) * jam_density1 );
                    all_ic.add(one_ic);
                }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return all_ic;

    }

    public static class RunParam {
        public String outfile_prefix;
        public double or_demand_vps;
        public double split;
        public double w;
        public double max_metering;
        public RunParam(String outfile_prefix,double or_demand_vps,double split,double w,double max_metering){
            this.outfile_prefix = outfile_prefix;
            this.or_demand_vps = or_demand_vps;
            this.split = split;
            this.w = w;
            this.max_metering = max_metering;
        }

        @Override
        public String toString() {
            return String.format("\n%s\t%f\t%f\t%f\t%f",outfile_prefix,or_demand_vps,split,w,max_metering);
        }
    }

}
