package edu.berkeley.path.lprm.rm;

import edu.berkeley.path.lprm.lp.problem.PointValue;
import edu.berkeley.path.lprm.lp.solver.ApacheSolver;
import edu.berkeley.path.lprm.lp.solver.Solver;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.fwy.FwySegment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public final class RampMeteringSolution {

    public enum OutputFormat {text,matlab};

    protected SegmentSolution [] Xopt;
    protected PointValue result;

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
//            case LPSOLVE:
//                solver = new LpSolveSolver();
//                break;
////            case GUROBI:
//                solver = new GurobiSolver();
//                break;
        }

        result = solver.solve(LP);

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

    public double get_cost(){
        return result.cost;
    }

    public ArrayList<double[]> get_matrix(String name){
        ArrayList<double[]> X = new ArrayList<double[]>();
        for(SegmentSolution segsol : Xopt)
            X.add(segsol.get(name));
        return X;
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
            if(name.equalsIgnoreCase("n"))
                return n;
            if(name.equalsIgnoreCase("l"))
                return l;
            if(name.equalsIgnoreCase("f"))
                return f;
            if(name.equalsIgnoreCase("r"))
                return r;
            return null;
        }
    }

    private static String getVar(String name,int seg_index,int timestep){
        return ProblemRampMetering.getVar(name,seg_index,timestep);
    }

    public void print_to_file(String filename,OutputFormat format) throws Exception {
        RampMeteringSolutionWriter writer = null;
        switch(format) {
            case matlab:
                writer = new RampMeteringSolutionWriterMAT();
                break;
            case text:
                writer = new RampMeteringSolutionWriterTXT();
                break;
        }
        writer.write_to_file(filename, this);
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

    @Override
    public String toString() {
        RampMeteringSolutionWriter writer = new RampMeteringSolutionWriterTXT();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writer.write_all_to_stream(stream,this);
        return stream.toString();
    }

}
