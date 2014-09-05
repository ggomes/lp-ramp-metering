package lp;

import lp.problem.PointValue;
import lp.solver.ApacheSolver;
import lp.solver.LpSolveSolver;
import lp.solver.Solver;
import lp.solver.SolverType;
import network.fwy.FwyNetwork;
import network.fwy.FwySegment;

import java.io.PrintWriter;
//import java.io.PrintWriter;


public final class RampMeteringSolution {

    public enum OutputFormat {text,matlab};
    protected SegmentSolution [] Xopt;
    protected int K;
    protected int I;
    protected double sim_dt;

    public RampMeteringSolution(ProblemRampMetering LP, FwyNetwork fwy, SolverType solver_type){

        this.I = fwy.num_segments;
        this.K = LP.K;
        this.sim_dt = LP.sim_dt_in_seconds;

        Solver solver = null;
        switch(solver_type){
            case APACHE:
                solver = new ApacheSolver();
                break;
            case LPSOLVE:
                solver = new LpSolveSolver();
                break;
//            case GUROBI:
//                solver = new GurobiSolver();
//                break;
        }

        PointValue result = solver.solve(LP);

        this.Xopt = new SegmentSolution[I];

        int i,k;
        for(i=0;i<I;i++){

            FwySegment seg = fwy.get_segment(i);

            Xopt[i] = new SegmentSolution(seg,K);

            Xopt[i].n[0] = seg.no;
            for(k=0;k<K;k++){
                Xopt[i].n[k+1] = result.get(getVar("n",i,k+1));
                Xopt[i].f[k] = result.get(getVar("f", i, k));
            }

            if(seg.is_metered){
                Xopt[i].l[0] = seg.lo;
                for(k=0;k<K;k++){
                    Xopt[i].l[k+1] = result.get(getVar("l",i,k+1));
                    Xopt[i].r[k] = result.get(getVar("r", i, k));
                }
            }

        }

    }

    public class SegmentSolution {
        protected double [] n;
        protected double [] l;
        protected double [] f;
        protected double [] r;

        public SegmentSolution(FwySegment fseg,int K){
            n = new double[K+1];
            f = new double[K];
            if(fseg.is_metered){
                l = new double[K+1];
                r = new double[K];
            }
        }
        public double [] get(String name){
            if(name.compareTo("n")==0)
                return n;
            if(name.compareTo("l")==0)
                return l;
            if(name.compareTo("r")==0)
                return r;
            if(name.compareTo("f")==0)
                return f;
            return null;
        }

    }

    private static String getVar(String name,int seg_index,int timestep){
        return ProblemRampMetering.getVar(name,seg_index,timestep);
    }



    /* Print solution to file */


//    public String print(String var,int seg_index){
//        return print(var,seg_index,false);
//    }
//
//    public String print(String var){
//        return print(var, OutputFormat.matlab);
//    }
//
//    public String print(){
//        return print(OutputFormat.matlab);
//    }


    /* Level 1 */

    public void print_to_file(String function_name,OutputFormat format) throws Exception {

        PrintWriter pw = null;
        switch(format) {

            case matlab:
                pw = new PrintWriter("out\\" + function_name + ".m");
                pw.print("function [n,l,f,r]=" + function_name + "()\n");
                break;

            case text:
//        WriteFile data = new WriteFile(function_name, true);
//        data.writeToFile(print(format));
                pw = new PrintWriter("out\\" + function_name + ".txt");
                break;
        }
        pw.print(print(format));
        pw.close();
    }

    /* Level 2 */

    public String print(OutputFormat format){
        return print("n",format)+"\n"+ print("f",format)+"\n"+ print("l",format)+"\n"+ print("r",format);
    }


    /* Level 3 */

    public String print(String var,OutputFormat format){
        String str = "";
        boolean isDensity;
        isDensity = var.compareTo("n")==0 || var.compareTo("l")==0;
        int Ka = isDensity ? K+1 : K;
        str = String.format("%s=nan(%d,%d);\n",var,I,Ka);
        for(int i=0;i<Xopt.length;i++)
            str = str.concat(print(var,i,format));
        return str;
    }

    /* Level 4 */

    public String print(String var,int seg_index,OutputFormat format){
        String str = "";
        boolean isDensity = var.compareTo("n")==0 || var.compareTo("l")==0;
        int lastK = isDensity ? K+1 : K;
        double [] x = Xopt[seg_index].get(var);
        if(x!=null)
            for(int k=0;k<lastK;k++){

                switch(format) {

                    case matlab:
                        str = str.concat(String.format("%s(%d,%d)=%.6f;\n",var,seg_index+1,k+1,x[k]));

                        break;

                    case text:
                        if(isDensity)
                            if (k%K !=0 || k ==0 )
                                str = str.concat(String.format("% .6f ",x[k]));
                            else
                                str = str.concat(String.format("% .6f\n",x[k]));
                        else
                            if (k%(K-1) !=0 || k ==0 )
                                str = str.concat(String.format("% .6f ",x[k]));
                            else
                                str = str.concat(String.format("% .6f\n",x[k]));

                        break;

                }

            }
        return str;
    }


//=====================================================================================================
//    public class WriteFile {
//        private String path;
//        private boolean append_to_file = false;
//
//        public WriteFile(String file_path) {
//            path = file_path;
//        }
//        public WriteFile(String file_path, boolean append_value) {
//            path = file_path;
//            append_to_file = append_value;
//        }
//
//        public void writeToFile(String textLine) throws IOException {
//            FileWriter write = new FileWriter(path,append_to_file);
//            PrintWriter print_line = new PrintWriter(write);
//            print_line.printf("%s" + "%n ", textLine);
//            print_line.close();
//        }
//    }



    @Override
    public String toString() {
        return print(OutputFormat.text);
    }



}
