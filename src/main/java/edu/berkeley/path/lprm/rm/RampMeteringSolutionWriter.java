package edu.berkeley.path.lprm.rm;

import java.util.ArrayList;

/**
 * Created by gomes on 9/10/2014.
 */
public abstract class RampMeteringSolutionWriter implements RampMerteringSolutionWriterInterface {

    public static String format_column(ArrayList<Long> x,int n,String delim){
        return format_row(x.toArray(new Long[x.size()]),n,delim);
    }

    public static String format_row(Long[] x, int n,String delim){
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

    public static String format_column(ArrayList<Long> x,String delim){
        return format_column(x.toArray(new Long[x.size()]),delim);
    }

    public static String format_column(Object[] x,String delim){
        String str = "";
        if(x!=null) {
            for (int i = 0; i < x.length; i++)
                str += x[i]+delim;
//            str += x[x.length - 1];
        }
        else {
            for (int i = 0; i < x.length; i++)
                str += "NaN"+delim;
            str += "NaN";
        }
        return str;
    }

    public static byte[] toBytes(String str){
        return str.getBytes();
    }

}
