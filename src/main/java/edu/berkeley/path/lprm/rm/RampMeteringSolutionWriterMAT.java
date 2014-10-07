package edu.berkeley.path.lprm.rm;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by gomes on 9/10/2014.
 */
public class RampMeteringSolutionWriterMAT extends RampMeteringSolutionWriter {

    @Override
    public void write_to_file(String filename, RampMeteringSolution rm){
        PrintStream ps=open_file(filename);
        ps.print("function [n,f,l,r]=" + filename + "()\n");
        write_all_to_stream(ps,rm);
        ps.close();
    }

    @Override
    public void write_all_to_stream(OutputStream ps,RampMeteringSolution rm){
        int K = rm.LP.K;
        write_to_stream(ps,rm,"n",K+1);
        write_to_stream(ps,rm,"f",K);
        write_to_stream(ps,rm,"l",K+1);
        write_to_stream(ps,rm,"r",K);
    }

    @Override
    public void write_to_stream(OutputStream ps,RampMeteringSolution rm,String varname,int numK){
        try {
            ArrayList<Double[]> matrix = rm.get_matrix(varname);
            ps.write(toBytes(varname+"=[...\n"));
            writeMatrix(ps, matrix, numK);
            ps.write(toBytes("];\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PrintStream open_file(String filename){
        PrintStream ps = null;
        try {
            ps = new PrintStream(new File("out\\" + filename + ".m"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ps;
    }

    private void writeMatrix(OutputStream ps,ArrayList<Double[]> matrix,int numTime) throws IOException {
        int i;
        for(i=0;i<matrix.size()-1;i++)
            if(matrix.get(i)!=null)
                ps.write(toBytes(format_row(matrix.get(i), numTime, ",") + "; ...\n"));
        i=matrix.size()-1;
        if(matrix.get(i)!=null)
            ps.write(toBytes(format_row(matrix.get(i), numTime, ",") + " ...\n"));
    }

}
