package edu.berkeley.path.lprm;

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

    public void print_config_data(RampMeteringSolution rm) throws IOException {
        ArrayList<Long> mainLineIds = rm.get_mainline_ids();
        String ml_ids_to_print = format_column(mainLineIds, "\n");
        String filename_ml_ids = path.concat("_ml_ids.txt");
        BatchWriter ml_ids_writer = new BatchWriter(filename_ml_ids,false);
        ml_ids_writer.writeToFile(ml_ids_to_print);

        ArrayList<Long> actuatedOnRampIds = rm.get_metered_onramp_ids();
        String act_or_ids_to_print = format_column(actuatedOnRampIds, "\n");
        String filename_act_or_ids = path.concat("_act_or_ids.txt");
        BatchWriter act_or_ids_writer = new BatchWriter(filename_act_or_ids,false);
        act_or_ids_writer.writeToFile(act_or_ids_to_print);

    }

    public void print_lp_results(RampMeteringSolution rm,int numK,int index) throws IOException {
        BatchWriter n_writer = new BatchWriter(path.concat("_"+Integer.toString(index)+"_n.txt"),true);
        ArrayList<Double[]> n_matrix = rm.get_matrix("n");
        for(int i=0;i<n_matrix.size();i++){
            if(n_matrix.get(i)!=null)
                n_writer.writeToFile(format_row(n_matrix.get(i), numK+1, "\t") + "\n");}

        BatchWriter f_writer = new BatchWriter(path.concat("_"+Integer.toString(index)+"_f.txt"),true);
        ArrayList<Double[]> f_matrix = rm.get_matrix("f");
        for(int i=0;i<f_matrix.size();i++){
            if(f_matrix.get(i)!=null)
                f_writer.writeToFile(format_row(f_matrix.get(i), numK, "\t") + "\n");}

        BatchWriter l_writer = new BatchWriter(path.concat("_"+Integer.toString(index)+"_l.txt"),true);
        ArrayList<Double[]> l_matrix = rm.get_matrix("l");
        for(int i=0;i<l_matrix.size();i++){
            if(l_matrix.get(i)!=null)
                l_writer.writeToFile(format_row(l_matrix.get(i), numK+1, "\t") + "\n");}

        BatchWriter r_writer = new BatchWriter(path.concat("_"+Integer.toString(index)+"_r.txt"),true);
        ArrayList<Double[]> r_matrix = rm.get_matrix("r");
        for(int i=0;i<r_matrix.size();i++){
            if(r_matrix.get(i)!=null)
                r_writer.writeToFile(format_row(r_matrix.get(i), numK, "\t") + "\n");}
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

    public static String format_row(Double[] x, int n,String delim){
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
