package edu.berkeley.path.lp;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by gomes on 9/10/2014.
 */
public class RampMeteringSolutionWriterTXT extends RampMeteringSolutionWriter {

    @Override
    public void write_to_file(String filename, RampMeteringSolution rm){

        PrintStream ps;

        ps=open_file(filename,"n");
        write_to_stream(ps,rm,"n", rm.K+1);
        ps.close();

        ps=open_file(filename,"f");
        write_to_stream(ps,rm,"f", rm.K);
        ps.close();

        ps=open_file(filename,"l");
        write_to_stream(ps,rm,"l", rm.K+1);
        ps.close();

        ps=open_file(filename,"r");
        write_to_stream(ps,rm,"r", rm.K);
        ps.close();
    }

    @Override
    public void write_all_to_stream(OutputStream ps,RampMeteringSolution rm){
        try {
            ps.write(toBytes("n:\n"));
            write_to_stream(ps, rm, "n", rm.K + 1);
            ps.write(toBytes("\nf:\n"));
            write_to_stream(ps, rm, "f", rm.K);
            ps.write(toBytes("\nl:\n"));
            write_to_stream(ps, rm, "l", rm.K + 1);
            ps.write(toBytes("\nr:\n"));
            write_to_stream(ps,rm,"r", rm.K);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write_to_stream(OutputStream ps,RampMeteringSolution rm,String varname,int numK){
        try {
            ArrayList<double[]> matrix = rm.get_matrix(varname);
            for(int i=0;i<matrix.size();i++)
                if(matrix.get(i)!=null)
                    ps.write(toBytes(format_row(matrix.get(i), numK, "\t") + "\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PrintStream open_file(String filename,String varname){
        PrintStream ps = null;
        try {
            ps = new PrintStream(new File("out\\" + filename + "_"+varname +".txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ps;
    }

}
