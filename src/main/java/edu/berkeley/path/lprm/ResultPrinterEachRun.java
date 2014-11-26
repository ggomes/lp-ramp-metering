package edu.berkeley.path.lprm;

import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.rm.RampMeteringSolution;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gomes on 10/6/14.
 */
public class ResultPrinterEachRun {

    String path;
    public ResultPrinterEachRun(String path_string){
        path = path_string;
    }

    public void print_config_data(FwyNetwork fwy) throws IOException {
        ArrayList<Long> mainLineIds = fwy.get_mainline_ids();
        String ml_ids_to_print = format_column(mainLineIds, "\n");
        String filename_ml_ids = path.concat("_ml_ids.txt");
        BatchWriter ml_ids_writer = new BatchWriter(filename_ml_ids);
        ml_ids_writer.write(ml_ids_to_print);
        ml_ids_writer.close();

        ArrayList<Long> actuatedOnRampIds = fwy.get_metered_onramp_ids();
        String act_or_ids_to_print = format_column(actuatedOnRampIds, "\n");
        String filename_act_or_ids = path.concat("_act_or_ids.txt");
        BatchWriter act_or_ids_writer = new BatchWriter(filename_act_or_ids);
        act_or_ids_writer.write(act_or_ids_to_print);
        act_or_ids_writer.close();

    }

    public void print_lp_results(RampMeteringSolution rm,int numK,int index) throws IOException {
        write_matrix_to_file(rm,numK,path,index,"n");
        write_matrix_to_file(rm,numK,path,index,"f");
        write_matrix_to_file(rm,numK,path,index,"l");
        write_matrix_to_file(rm,numK,path,index,"r");
    }

    private static void write_matrix_to_file(RampMeteringSolution rm,int numK,String path,int index,String var) throws IOException {
        BatchWriter writer = new BatchWriter(String.format("%s_%d_%s.txt",path,index,var));
        ArrayList<double[]> matrix = rm.get_matrix(var);
        for(int i=0;i<matrix.size();i++)
            if(matrix.get(i)!=null)
                writer.write(format_row(matrix.get(i), numK+1, "\t") + "\n");
        writer.close();
    }

    public static String format_column(ArrayList<Long> x,String delim){
        return format_column(x.toArray(new Long[x.size()]),delim);
    }

    public static String format_column(Object[] x,String delim){
        String str = "";
        if(x!=null) {
            for (int i = 0; i < x.length; i++)
                str += x[i]+delim;
        }
        else {
            for (int i = 0; i < x.length; i++)
                str += "NaN"+delim;
            str += "NaN";
        }
        return str;
    }

    public static String format_row(double[] x, int n,String delim){
        String str = "";
        if(x!=null) {
            for (int i = 0; i < x.length - 1; i++)
                str += x[i]+delim;
            str += x[x.length - 1];
        }
        else {
            for (int i = 0; i < n - 1; i++)
                str += "NaN"+delim;
            str += "NaN";
        }
        return str;
    }
}
