package edu.berkeley.path.lprm.rm;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by gomes on 9/10/2014.
 */
public class RampMeteringSolutionWriterTXT extends RampMeteringSolutionWriter {

    protected enum network_data{
        fr,
        actuatedOr,
        ml;
    }

    @Override
    public void write_to_file(String filename, RampMeteringSolution rm){

        int K = rm.LP.K;
        PrintStream ps;

        ps=open_file(filename,"n");
        write_to_stream(ps,rm,"n", K+1);
        ps.close();

        ps=open_file(filename,"f");
        write_to_stream(ps,rm,"f", K);
        ps.close();

        ps=open_file(filename,"l");
        write_to_stream(ps,rm,"l", K+1);
        ps.close();

        ps=open_file(filename,"r");
        write_to_stream(ps,rm,"r", K);
        ps.close();

//        ps=open_file(filename,"off_ramp_Ids");
//        write_ids_to_stream(ps,rm,network_data.fr);
//        ps.close();
//
//        ps=open_file(filename,"main_line_ids");
//        write_ids_to_stream(ps,rm,network_data.ml);
//        ps.close();
//
//        ps=open_file(filename,"actuated_on_ramp_Ids");
//        write_ids_to_stream(ps,rm,network_data.actuatedOr);
//        ps.close();
//
//
//
//        ps=open_file(filename,"sim_dt");
//        write_to_stream_simdt(ps,rm);
//        ps.close();


        ps=open_file(filename,"main_line_ids");
        write_to_stream_mainline_ids(ps,rm);
        ps.close();

        ps=open_file(filename,"actuated_on_ramp_Ids");
        write_to_stream_or_ids(ps,rm);
        ps.close();

        ps=open_file(filename,"off_ramp_Ids");
        write_to_stream_fr_ids(ps,rm);
        ps.close();

        ps=open_file(filename,"sim_dt");
        write_to_stream_simdt(ps,rm);
        ps.close();


    }

    @Override
    public void write_all_to_stream(OutputStream ps,RampMeteringSolution rm){
        try {
            int K = rm.LP.K;
            ps.write(toBytes("n:\n"));
            write_to_stream(ps, rm, "n", K + 1);
            ps.write(toBytes("\nf:\n"));
            write_to_stream(ps, rm, "f", K);
            ps.write(toBytes("\nl:\n"));
            write_to_stream(ps, rm, "l", K + 1);
            ps.write(toBytes("\nr:\n"));
            write_to_stream(ps,rm,"r", K);
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

    public void write_ids_to_stream(OutputStream ps,RampMeteringSolution rm,network_data linkType) {
        try {
            switch (linkType) {
                case ml:


                ps.write(toBytes(format_column(rm.get_mainline_ids(), "\n")));
                    break;
                case fr:
                    ps.write(toBytes(format_column(rm.get_offramp_ids(), "\n")));
                    break;
                case actuatedOr:
                    ps.write(toBytes(format_column(rm.get_metered_onramp_ids(), "\n")));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write_to_stream_mainline_ids(OutputStream ps,RampMeteringSolution rm){
        try {
            ps.write(toBytes(format_column(rm.get_mainline_ids(),"\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write_to_stream_or_ids(OutputStream ps,RampMeteringSolution rm){
        try {
            ps.write(toBytes(format_column(rm.get_metered_onramp_ids(),"\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write_to_stream_fr_ids(OutputStream ps,RampMeteringSolution rm){
        try {
            ps.write(toBytes(format_column(rm.get_offramp_ids(),"\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write_to_stream_simdt(OutputStream ps,RampMeteringSolution rm){
        try {
            double simdt = rm.getSim_dt();
            ps.write(toBytes((simdt+"\n")));
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
