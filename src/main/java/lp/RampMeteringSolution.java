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
//    public enum OutputToWrite {n,f,l,r};
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
    /* Level 0 */
//public void print_to_file_all_outputs(String function_name,OutputFormat format)throws  Exception{
//    print_to_file_specified_output(function_name,format,"n");
//    print_to_file_specified_output(function_name,format,"f");
//    print_to_file_specified_output(function_name,format,"r");
//    print_to_file_specified_output(function_name,format,"l");
//}


    /* Level 1 */

//    public void print_to_file_specified_output(String function_name,OutputFormat format,String var) throws Exception {
//
//        String matlabFunctionHeader = "";
//        if(var == "f")
//        {
//                function_name = function_name.concat("_f");
//                matlabFunctionHeader = matlabFunctionHeader.concat("function [f]=");}
//        else if (var == "n"){
//
//                function_name = function_name.concat("_n");
//                matlabFunctionHeader = matlabFunctionHeader.concat("function [n]=");}
//        else if (var == "r"){
//               function_name = function_name.concat("_r");
//               matlabFunctionHeader = matlabFunctionHeader.concat("function [r]=");}
//        else {
//               function_name = function_name.concat("_l");
//                matlabFunctionHeader = matlabFunctionHeader.concat("function [l]=");}
//
//
//        PrintWriter pw = null;
//        switch(format) {
//
//            case matlab:
//
//
//                pw = new PrintWriter("out\\" + function_name + ".m");
//                pw.print(matlabFunctionHeader + function_name + "()\n");
//                break;
//
//            case text:
////        WriteFile data = new WriteFile(function_name, true);
////        data.writeToFile(print(format));
//                pw = new PrintWriter("out\\" + function_name + ".txt");
//                break;
//        }
//        pw.print(print(format,var));
//        pw.close();
//    }

//    public void print_to_file(String function_name,OutputFormat format) throws Exception {
//
//        PrintWriter pw = null;
//        switch(format) {
//
//            case matlab:
//                pw = new PrintWriter("out\\" + function_name + ".m");
//                pw.print("function [n,f,l,r]=" + function_name + "()\n");
//                break;
//
//            case text:
////        WriteFile data = new WriteFile(function_name, true);
////        data.writeToFile(print(format));
//                pw = new PrintWriter("out\\" + function_name + ".txt");
//                break;
//        }
//        pw.print(print(format));
//        pw.close();
//    }

    public void print_to_file(String function_name,OutputFormat format) throws Exception {
        PrintWriter pw = null;
        switch(format) {

            case matlab:
                pw = new PrintWriter("out\\" + function_name + ".m");
                pw.print("function [n,f,l,r]=" + function_name + "()\n");
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

//    public String print(OutputFormat format,String var){
//        return print(var,format);
//    }

    public String print(OutputFormat format){
        return print("n",format)+"\n"+ print("f",format)+"\n"+ print("l",format)+"\n"+ print("r",format);
    }


    /* Level 3 */

    public String print(String var,OutputFormat format){
        String str = "";
        boolean isDensity;
        isDensity = var.compareTo("n")==0 || var.compareTo("l")==0;
        int Ka = isDensity ? K+1 : K;
        //str = String.format("%s=nan(%d,%d);\n",var,I,Ka);
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


    public boolean[][] is_free_flow_CTM(FwyNetwork fwy){

        boolean [][] isFreeFlowCTM = new boolean[I][K];
        double rhs;

        for(int i=0;i<fwy.num_segments;i++){

            FwySegment seg = fwy.get_segment(i);

            double vf = seg.get_vf_link_per_sec()*sim_dt;

            double [] f = Xopt[i].get("f");
            double [] n = Xopt[i].get("n");
            double [] r = Xopt[i].get("r");

            // LHS
            // {always}           {k>0}                       {metered}
            // f[i][k] -betabar[i][k]*v[i]*n[i][k] - betabar[i][k]*v[i]*gamma*r[i][k]

            for(int k=0;k<K;k++){

                double time = k*sim_dt;
                double betabar = 1-seg.get_split_ratio(time);
                double d = seg.get_demand_in_vps(time)*sim_dt;
                double no = k==0? seg.no : 0;

                double constraint = f[k];
                if(k>0 && betabar>0)
                    constraint += -betabar*vf*n[k];
                if(seg.is_metered && betabar>0)
                    constraint += -betabar*vf*fwy.gamma*r[k];

                rhs = betabar*vf*no;
                rhs += !seg.is_metered ? betabar*vf*fwy.gamma*d : 0;

                double epsilon = Math.pow(10,-6);
                if (Math.abs(constraint-rhs) <= epsilon){
                    isFreeFlowCTM[i][k] = true;
                }
            }
        }
        return isFreeFlowCTM;
    }



    public boolean[][] is_flow_CTM_behavior(FwyNetwork fwy){

        boolean [][] isFlowCTMbehavior = new boolean[I][K];
        double rhs;
        for(int i=0;i<fwy.num_segments;i++){

            FwySegment seg = fwy.get_segment(i);

            double vf = seg.get_vf_link_per_sec()*sim_dt;
            double f_max = seg.get_fmax_vps()*sim_dt;

            double [] f = Xopt[i].get("f");
            double [] n = Xopt[i].get("n");
            double [] r = Xopt[i].get("r");
//            if (i <= fwy.num_segments-1) {
//                double[] nNext = Xopt[i+1].get("n");
//                double[] rNext = Xopt[i+1].get("r");
//            }

            double epsilon = Math.pow(10,-7);

            for(int k=0; k<K; k++){
                double time = k*sim_dt;
                double betabar = 1-seg.get_split_ratio(time);
                double d = seg.get_demand_in_vps(time)*sim_dt;
                double no = k==0? seg.no : 0;

                // Checking if it satisfies main-line free flow
                // LHS
                // {always}           {k>0}                       {metered}
                // f[i][k] -betabar[i][k]*v[i]*n[i][k] - betabar[i][k]*v[i]*gamma*r[i][k]

                double constraintFFlow = f[k];
                if(k>0 && betabar>0)
                    constraintFFlow += -betabar*vf*n[k];
                if(seg.is_metered && betabar>0)
                    constraintFFlow += -betabar*vf*fwy.gamma*r[k];
                rhs = betabar*vf*no;
                rhs += !seg.is_metered ? betabar*vf*fwy.gamma*d : 0;

                if (Math.abs(constraintFFlow-rhs) <= epsilon){
                    isFlowCTMbehavior[i][k] = true;
                }

                if (isFlowCTMbehavior[i][k] == false){
                    // Checking if it satisfies main line congestion
                    if(i<fwy.num_segments-1){
                        FwySegment next_seg = fwy.get_segment(i+1);;
                        double next_w = next_seg.get_w_link_per_sec()*sim_dt;
                        double next_d = next_seg.get_demand_in_vps(time)*sim_dt;
                        double next_no = k==0? next_seg.no : 0;
                        double[] nNext = Xopt[i+1].get("n");
                        double[] rNext = Xopt[i+1].get("r");


                        // LHS
                        // {always}       {k>0}                {metered}
                        // f[i][k] + w[i+1]*n[i+1][k] + w[i+1]*gamma*r[i+1][k]
                        double constraintMLCong = f[k];

                        if(k>0)
                            constraintMLCong += next_w*nNext[k];
                        if(next_seg.is_metered)
                            constraintMLCong += next_w*fwy.gamma*rNext[k];

                        // RHS
                        //     {always}              {k=0}             {!metered}
                        // w[i+1]*njam[i+1] - w[i+1]*n[i+1][0] - w[i+1]*gamma*d[i+1][k]
                        rhs = next_w*next_seg.n_max;
                        rhs += -next_w*next_no;
                        rhs += !next_seg.is_metered ? -next_w*fwy.gamma*next_d : 0;

                        if (Math.abs(constraintMLCong-rhs) <= epsilon){
                            isFlowCTMbehavior[i][k] = true;
                        }
                    }
                    if (isFlowCTMbehavior[i][k] == false){

                        //  Checking Main-line Capacity Condition
                        double constraintCapacity = f[k];
                        if (Math.abs(constraintCapacity-f_max) <= epsilon){
                            isFlowCTMbehavior[i][k] = true;
                        }
                    }

                }


            }

        }
        return isFlowCTMbehavior;
    }




    public boolean[][] is_congestion_flow_CTM(FwyNetwork fwy){

        boolean [][] isCongestionFlowCTM = new boolean[I][K];
        double rhs;
        for(int i=0;i<fwy.num_segments;i++){

            FwySegment seg = fwy.get_segment(i);

            double vf = seg.get_vf_link_per_sec()*sim_dt;
            double f_max = seg.get_fmax_vps()*sim_dt;

            double [] f = Xopt[i].get("f");
            double [] n = Xopt[i].get("n");
            double [] r = Xopt[i].get("r");
//            if (i <= fwy.num_segments-1) {
//                double[] nNext = Xopt[i+1].get("n");
//                double[] rNext = Xopt[i+1].get("r");
//            }

            double epsilon = Math.pow(10,-6);

            for(int k=0; k<K; k++){
                double time = k*sim_dt;
                double betabar = 1-seg.get_split_ratio(time);
                double d = seg.get_demand_in_vps(time)*sim_dt;
                double no = k==0? seg.no : 0;

                // Checking if it satisfies main line congestion
                if(i<fwy.num_segments-1){
                    FwySegment next_seg = fwy.get_segment(i+1);;
                    double next_w = next_seg.get_w_link_per_sec()*sim_dt;
                    double next_d = next_seg.get_demand_in_vps(time)*sim_dt;
                    double next_no = k==0? next_seg.no : 0;
                    double[] nNext = Xopt[i+1].get("n");
                    double[] rNext = Xopt[i+1].get("r");


                    // LHS
                    // {always}       {k>0}                {metered}
                    // f[i][k] + w[i+1]*n[i+1][k] + w[i+1]*gamma*r[i+1][k]
                    double constraintMLCong = f[k];

                    if(k>0)
                        constraintMLCong += next_w*nNext[k];
                    if(next_seg.is_metered)
                        constraintMLCong += next_w*fwy.gamma*rNext[k];

                    // RHS
                    //     {always}              {k=0}             {!metered}
                    // w[i+1]*njam[i+1] - w[i+1]*n[i+1][0] - w[i+1]*gamma*d[i+1][k]
                    rhs = next_w*next_seg.n_max;
                    rhs += -next_w*next_no;
                    rhs += !next_seg.is_metered ? -next_w*fwy.gamma*next_d : 0;

                    if (Math.abs(constraintMLCong-rhs) <= epsilon){
                        isCongestionFlowCTM[i][k] = true;
                    }
                }

            }

        }
        return isCongestionFlowCTM;
    }


    public boolean[][] is_max_flow_CTM(FwyNetwork fwy){

        boolean [][] isMAxFlowCTM = new boolean[I][K];
        for(int i=0;i<fwy.num_segments;i++){

            FwySegment seg = fwy.get_segment(i);
            double f_max = seg.get_fmax_vps()*sim_dt;

            double [] f = Xopt[i].get("f");

//            if (i <= fwy.num_segments-1) {
//                double[] nNext = Xopt[i+1].get("n");
//                double[] rNext = Xopt[i+1].get("r");
//            }

            double epsilon = Math.pow(10,-6);

            for(int k=0; k<K; k++){
                double time = k*sim_dt;
                double constraintCapacity = f[k];
                if (Math.abs(constraintCapacity-f_max) <= epsilon){
                    isMAxFlowCTM[i][k] = true;
                }
            }

        }
        return  isMAxFlowCTM;
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


//
//    @Override
//    public String toString() {
//        return print(OutputFormat.text);
//    }


    @Override
    public String toString() {
        return print(OutputFormat.text);
    }


}
