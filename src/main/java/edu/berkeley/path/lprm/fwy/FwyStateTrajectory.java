package edu.berkeley.path.lprm.fwy;

import java.io.*;

/**
 * Created by gomes on 10/20/2014.
 */
public class FwyStateTrajectory {
    public double dt;
    public int num_steps;
    public FwyNetwork fwy;
    public double [][] n;
    public double [][] f;
    public double [][] r;
    public double [][] l;
    public FwyStateTrajectory(FwyNetwork fwy,double dt,int num_steps){
        this.fwy = fwy;
        this.dt = dt;
        this.num_steps = num_steps;
        n = new double [fwy.num_segments][num_steps+1];
        f = new double [fwy.num_segments][num_steps];
        l = new double [fwy.num_segments][num_steps+1];
        r = new double [fwy.num_segments][num_steps];

        // inital condition
        for(int i=0;i<fwy.num_segments;i++){
            n[i][0] = fwy.segments.get(i).no;
            l[i][0] = fwy.segments.get(i).lo;
        }
    }

    public double get_ml_tvh(){
        return sumsum(n)*dt/3600d;
    }

    public double get_or_tvh(){
        return sumsum(l)*dt/3600d;
    }

    public double get_tvh(){
        return (sumsum(n)+sumsum(l))*dt/3600d;
    }

    public double get_tvm(){
        return (sumsum(f)+sumsum(r))*dt/3600d;
    }

    public void print_to_file(String filename){
        PrintStream ps=open_file(filename);
        ps.print("function [n,f,l,r]=" + filename + "()\n");
        write_to_stream(ps,n,"n");
        write_to_stream(ps,f,"f");
        write_to_stream(ps,l,"l");
        write_to_stream(ps,r,"r");
        ps.close();
    }

    public void write_to_stream(OutputStream ps,double [][] x,String varname){
        try {
            ps.write(toBytes(varname+"=[...\n"));
            writeMatrix(ps, x);
            ps.write(toBytes("];\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMatrix(OutputStream ps,double [][] x) throws IOException {
        int i;
        for(i=0;i<x.length-1;i++)
            ps.write(toBytes(format_row(x[i], ",") + "; ...\n"));
        i=x.length-1;
        if(x[i]!=null)
            ps.write(toBytes(format_row(x[i], ",") + " ...\n"));
    }

    public static String format_row(double[] x,String delim){
        String str = "";
        if(x!=null) {
            for (int i = 0; i < x.length-1; i++)
                str += x[i]+delim;
            str += x[x.length-1];
        }
        else {
            for (int i = 0; i < x.length-1; i++)
                str += "NaN"+delim;
            str += "NaN";
        }
        return str;
    }

    public static byte[] toBytes(String str){
        return str.getBytes();
    }

    private PrintStream open_file(String filename){
        PrintStream ps = null;
        try {
            ps = new PrintStream(new File(filename + ".m"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ps;
    }

    private static double sumsum(double [][]x){
        if(x==null)
            return 0d;
        int i,j;
        double sum = 0d;
        for(i=0;i<x.length;i++)
            for(j=0;j<x[i].length;j++)
                sum += x[i][j];
        return sum;
    }
}
