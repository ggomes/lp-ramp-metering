package edu.berkeley.path.lprm.rm;

/**
 * Created by gomes on 9/10/2014.
 */
public abstract class RampMeteringSolutionWriter implements RampMerteringSolutionWriterInterface {

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

    public static byte[] toBytes(String str){
        return str.getBytes();
    }

}
