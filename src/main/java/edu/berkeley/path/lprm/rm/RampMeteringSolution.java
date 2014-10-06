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

    protected PointValue result;
    protected SegmentSolution [] Xopt;      // unpacked result in a convenient format

    protected boolean is_ctm;

    public double[] mainLineIDs;
    public double[] actuatedOnRampIDs;
    public double[] offRampIDs;
    public double TVH;
    public double TVM;

    public int K;
    protected int I;
    protected double sim_dt;

    public RampMeteringSolution(ProblemRampMetering LP, FwyNetwork fwy, SolverType solver_type){

        this.I = fwy.num_segments;
        this.K = LP.K;
        this.sim_dt = LP.sim_dt_in_seconds;

        mainLineIDs = new double[I];
        actuatedOnRampIDs = new double[fwy.num_actuated_ors];
        offRampIDs = new double[fwy.num_frs];

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

        result = solver.solve(LP);

        is_ctm = false;
//        ...

        double epsilon = Math.pow(10,-6);
        HashMap<String,Problem.ConstraintState> constraint_evaluation = LP.evaluate_constraints(result,epsilon);
        HashMap<String,Problem.ConstraintState> lower_bounds_evaluation = LP.evaluate_lower_bounds(result,epsilon);
        HashMap<String,Problem.ConstraintState> upper_bounds_evaluation = LP.evaluate_upper_bounds(result,epsilon);

        HashMap<String,Problem.ConstraintState> CTM_behavior = new HashMap<String, Problem.ConstraintState>();
        HashMap<String,Problem.ConstraintState> not_CTM_behavior = new HashMap<String, Problem.ConstraintState>();
        HashMap<String,Problem.ConstraintState> cnst_violated = new HashMap<String, Problem.ConstraintState>();

        for (int i=0;i<I;i++){
            for (int k=0;k<K;k++){
                String ml_free_flow_key = "MLFLW_FF";
                String ml_cong_flow_key = "MLFLW_CNG";
                String ml_capacity_flow_key = "f";
                String ml_not_CTM_key = "not_CTM_";
                String ml_cons_key = "MLCONS";
                String index_string = "[" + Integer.toString(i)+ "]" + "[" + Integer.toString(k) + "]";

                ml_free_flow_key = ml_free_flow_key.concat(index_string);
                ml_cong_flow_key = ml_cong_flow_key.concat(index_string);
                ml_capacity_flow_key = ml_capacity_flow_key.concat(index_string);
                ml_cons_key = ml_cons_key.concat(index_string);

                ml_not_CTM_key = ml_not_CTM_key.concat(index_string);

//                ml_cnstr_violated = ml_cnstr_violated.concat(index_string);

                Problem.ConstraintState ml_FFL_const = constraint_evaluation.get(ml_free_flow_key);
                Problem.ConstraintState ml_CNG_const = constraint_evaluation.get(ml_cong_flow_key);
                Problem.ConstraintState ml_cap_const = upper_bounds_evaluation.get(ml_capacity_flow_key);
                Problem.ConstraintState ml_cons_const = constraint_evaluation.get(ml_cons_key);

                if (ml_FFL_const == Problem.ConstraintState.active)
                    CTM_behavior.put(ml_free_flow_key,ml_FFL_const);
                else if (ml_CNG_const == Problem.ConstraintState.active)
                    CTM_behavior.put(ml_cong_flow_key, Problem.ConstraintState.active);
                else if (ml_cap_const == Problem.ConstraintState.active)
                    CTM_behavior.put(ml_capacity_flow_key, Problem.ConstraintState.active);
                else if (ml_FFL_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_free_flow_key, Problem.ConstraintState.inactive);
                else if (ml_CNG_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_cong_flow_key, Problem.ConstraintState.violated);
                else if (ml_cap_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_capacity_flow_key, Problem.ConstraintState.violated);
                else
                    not_CTM_behavior.put(ml_not_CTM_key,Problem.ConstraintState.inactive);

                if (ml_cons_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_cons_key, Problem.ConstraintState.violated);



//                if (ml_FFL_const == Problem.ConstraintState.active)
//                    CTM_behavior.put(ml_free_flow_key,ml_FFL_const);
//                else if (ml_CNG_const == Problem.ConstraintState.active)
//                    CTM_behavior.put(ml_cong_flow_key, Problem.ConstraintState.active);
//                else if (ml_cap_const == Problem.ConstraintState.active)
//                    CTM_behavior.put(ml_capacity_flow_key, Problem.ConstraintState.active);
//                else
//                    not_CTM_behavior.put(ml_not_CTM_key, Problem.ConstraintState.inactive);
                }
            }

        if (not_CTM_behavior.isEmpty())
            is_ctm = true;

        this.Xopt = new SegmentSolution[I];

        int i,k;
        int actuatedORCounter = 0;
        int frCounter = 0;
        TVM = 0;
        TVH = 0;

        for(i=0;i<I;i++){

            FwySegment seg = fwy.get_segment(i);


            Xopt[i] = new SegmentSolution(seg,K);

            mainLineIDs[i] = seg.get_main_line_link_id();

            if (seg.is_metered){
                actuatedOnRampIDs[actuatedORCounter] = seg.get_actuated_on_ramp_id();
                actuatedORCounter += 1;
            }

            if (seg.has_off_ramp){
                offRampIDs[i] = seg.get_off_ramp_id();
                frCounter += 1;
            }


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



            for (k=0;k<K;k++) {
                double d = seg.get_demand_in_vps(k)*sim_dt;
                TVH += result.get(getVar("n",i,k+1));
                if (seg.has_off_ramp)
                    TVM += result.get(getVar("f", i, k)) / (1 - seg.get_split_ratio(k));
                else
                    TVM += result.get(getVar("f", i, k));

                if (seg.has_on_ramp){
                    TVM += (seg.is_metered? result.get(getVar("f", i, k)) : d);
                    TVH += (seg.is_metered? result.get(getVar("l",i,k+1)) : 0);
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


    public double[] get_ml_ids(){
        return mainLineIDs;
    }

    public double[] get_actuated_or_ids(){
        return actuatedOnRampIDs;
    }

    public double[] get_fr_ids(){
        return offRampIDs;
    }

    public double getSim_dt(){
        return sim_dt;
    }

    public double getTVH(){return  TVM;}

    public double getTVM(){return TVH;}

    public PointValue get_PointValue(){return result;}


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

    public void print_to_file_lp_results(String filename){
        RampMeteringSolutionWriter writer = null;
        writer = new RampMeteringSolutionWriterTXT();


    }

    public void print_to_file(String filename,OutputFormat format){
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

//    public void print_to_textfile_ids(String filename,OutputFormat format){
//        RampMeteringSolutionWriter writer = null;
//        writer = new RampMeteringSolutionWriterTXT();
//        writer.
////        writer.write_to_file(filename, this);
//    }






    public boolean is_CTM(){
        return is_ctm;
    }


//    public boolean[][] is_free_flow_CTM(FwyNetwork fwy){
//
//        boolean [][] isFreeFlowCTM = new boolean[I][K];
//        double rhs;
//
//        for(int i=0;i<fwy.num_segments;i++){
//
//            FwySegment seg = fwy.get_segment(i);
//
//            double vf = seg.get_vf_link_per_sec()*sim_dt;
//
//            double [] f = Xopt[i].get("f");
//            double [] n = Xopt[i].get("n");
//            double [] r = Xopt[i].get("r");
//
//            // LHS
//            // {always}           {k>0}                       {metered}
//            // f[i][k] -betabar[i][k]*v[i]*n[i][k] - betabar[i][k]*v[i]*gamma*r[i][k]
//
//            for(int k=0;k<K;k++){
//
//                double time = k*sim_dt;
//                double betabar = 1-seg.get_split_ratio(time);
//                double d = seg.get_demand_in_vps(time)*sim_dt;
//                double no = k==0? seg.no : 0;
//
//                double constraint = f[k];
//                if(k>0 && betabar>0)
//                    constraint += -betabar*vf*n[k];
//                if(seg.is_metered && betabar>0)
//                    constraint += -betabar*vf*fwy.gamma*r[k];
//
//                rhs = betabar*vf*no;
//                rhs += !seg.is_metered ? betabar*vf*fwy.gamma*d : 0;
//
//                double epsilon = Math.pow(10,-6);
//                if (Math.abs(constraint-rhs) <= epsilon){
//                    isFreeFlowCTM[i][k] = true;
//                }
//            }
//        }
//        return isFreeFlowCTM;
//    }

//    public boolean[][] is_flow_CTM_behavior(FwyNetwork fwy){
//
//        boolean [][] isFlowCTMbehavior = new boolean[I][K];
//        double rhs;
//        for(int i=0;i<fwy.num_segments;i++){
//
//            FwySegment seg = fwy.get_segment(i);
//
//            double vf = seg.get_vf_link_per_sec()*sim_dt;
//            double f_max = seg.get_fmax_vps()*sim_dt;
//
//            double [] f = Xopt[i].get("f");
//            double [] n = Xopt[i].get("n");
//            double [] r = Xopt[i].get("r");
////            if (i <= fwy.num_segments-1) {
////                double[] nNext = Xopt[i+1].get("n");
////                double[] rNext = Xopt[i+1].get("r");
////            }
//
//            double epsilon = Math.pow(10,-7);
//
//            for(int k=0; k<K; k++){
//                double time = k*sim_dt;
//                double betabar = 1-seg.get_split_ratio(time);
//                double d = seg.get_demand_in_vps(time)*sim_dt;
//                double no = k==0? seg.no : 0;
//
//                // Checking if it satisfies main-line free flow
//                // LHS
//                // {always}           {k>0}                       {metered}
//                // f[i][k] -betabar[i][k]*v[i]*n[i][k] - betabar[i][k]*v[i]*gamma*r[i][k]
//
//                double constraintFFlow = f[k];
//                if(k>0 && betabar>0)
//                    constraintFFlow += -betabar*vf*n[k];
//                if(seg.is_metered && betabar>0)
//                    constraintFFlow += -betabar*vf*fwy.gamma*r[k];
//                rhs = betabar*vf*no;
//                rhs += !seg.is_metered ? betabar*vf*fwy.gamma*d : 0;
//
//                if (Math.abs(constraintFFlow-rhs) <= epsilon){
//                    isFlowCTMbehavior[i][k] = true;
//                }
//
//                if (isFlowCTMbehavior[i][k] == false){
//                    // Checking if it satisfies main line congestion
//                    if(i<fwy.num_segments-1){
//                        FwySegment next_seg = fwy.get_segment(i+1);;
//                        double next_w = next_seg.get_w_link_per_sec()*sim_dt;
//                        double next_d = next_seg.get_demand_in_vps(time)*sim_dt;
//                        double next_no = k==0? next_seg.no : 0;
//                        double[] nNext = Xopt[i+1].get("n");
//                        double[] rNext = Xopt[i+1].get("r");
//
//
//                        // LHS
//                        // {always}       {k>0}                {metered}
//                        // f[i][k] + w[i+1]*n[i+1][k] + w[i+1]*gamma*r[i+1][k]
//                        double constraintMLCong = f[k];
//
//                        if(k>0)
//                            constraintMLCong += next_w*nNext[k];
//                        if(next_seg.is_metered)
//                            constraintMLCong += next_w*fwy.gamma*rNext[k];
//
//                        // RHS
//                        //     {always}              {k=0}             {!metered}
//                        // w[i+1]*njam[i+1] - w[i+1]*n[i+1][0] - w[i+1]*gamma*d[i+1][k]
//                        rhs = next_w*next_seg.n_max;
//                        rhs += -next_w*next_no;
//                        rhs += !next_seg.is_metered ? -next_w*fwy.gamma*next_d : 0;
//
//                        if (Math.abs(constraintMLCong-rhs) <= epsilon){
//                            isFlowCTMbehavior[i][k] = true;
//                        }
//                    }
//                    if (isFlowCTMbehavior[i][k] == false){
//
//                        //  Checking Main-line Capacity Condition
//                        double constraintCapacity = f[k];
//                        if (Math.abs(constraintCapacity-f_max) <= epsilon){
//                            isFlowCTMbehavior[i][k] = true;
//                        }
//                    }
//
//                }
//
//
//            }
//
//        }
//        return isFlowCTMbehavior;
//    }

//    public boolean[][] is_congestion_flow_CTM(FwyNetwork fwy){
//
//        boolean [][] isCongestionFlowCTM = new boolean[I][K];
//        double rhs;
//        for(int i=0;i<fwy.num_segments;i++){
//
//            FwySegment seg = fwy.get_segment(i);
//
//            double vf = seg.get_vf_link_per_sec()*sim_dt;
//            double f_max = seg.get_fmax_vps()*sim_dt;
//
//            double [] f = Xopt[i].get("f");
//            double [] n = Xopt[i].get("n");
//            double [] r = Xopt[i].get("r");
////            if (i <= fwy.num_segments-1) {
////                double[] nNext = Xopt[i+1].get("n");
////                double[] rNext = Xopt[i+1].get("r");
////            }
//
//            double epsilon = Math.pow(10,-6);
//
//            for(int k=0; k<K; k++){
//                double time = k*sim_dt;
//                double betabar = 1-seg.get_split_ratio(time);
//                double d = seg.get_demand_in_vps(time)*sim_dt;
//                double no = k==0? seg.no : 0;
//
//                // Checking if it satisfies main line congestion
//                if(i<fwy.num_segments-1){
//                    FwySegment next_seg = fwy.get_segment(i+1);;
//                    double next_w = next_seg.get_w_link_per_sec()*sim_dt;
//                    double next_d = next_seg.get_demand_in_vps(time)*sim_dt;
//                    double next_no = k==0? next_seg.no : 0;
//                    double[] nNext = Xopt[i+1].get("n");
//                    double[] rNext = Xopt[i+1].get("r");
//
//
//                    // LHS
//                    // {always}       {k>0}                {metered}
//                    // f[i][k] + w[i+1]*n[i+1][k] + w[i+1]*gamma*r[i+1][k]
//                    double constraintMLCong = f[k];
//
//                    if(k>0)
//                        constraintMLCong += next_w*nNext[k];
//                    if(next_seg.is_metered)
//                        constraintMLCong += next_w*fwy.gamma*rNext[k];
//
//                    // RHS
//                    //     {always}              {k=0}             {!metered}
//                    // w[i+1]*njam[i+1] - w[i+1]*n[i+1][0] - w[i+1]*gamma*d[i+1][k]
//                    rhs = next_w*next_seg.n_max;
//                    rhs += -next_w*next_no;
//                    rhs += !next_seg.is_metered ? -next_w*fwy.gamma*next_d : 0;
//
//                    if (Math.abs(constraintMLCong-rhs) <= epsilon){
//                        isCongestionFlowCTM[i][k] = true;
//                    }
//                }
//
//            }
//
//        }
//        return isCongestionFlowCTM;
//    }

//    public boolean[][] is_max_flow_CTM(FwyNetwork fwy){
//
//        boolean [][] isMAxFlowCTM = new boolean[I][K];
//        for(int i=0;i<fwy.num_segments;i++){
//
//            FwySegment seg = fwy.get_segment(i);
//            double f_max = seg.get_fmax_vps()*sim_dt;
//
//            double [] f = Xopt[i].get("f");
//
////            if (i <= fwy.num_segments-1) {
////                double[] nNext = Xopt[i+1].get("n");
////                double[] rNext = Xopt[i+1].get("r");
////            }
//
//            double epsilon = Math.pow(10,-6);
//
//            for(int k=0; k<K; k++){
//                double time = k*sim_dt;
//                double constraintCapacity = f[k];
//                if (Math.abs(constraintCapacity-f_max) <= epsilon){
//                    isMAxFlowCTM[i][k] = true;
//                }
//            }
//
//        }
//        return  isMAxFlowCTM;
//    }



    @Override
    public String toString() {
        RampMeteringSolutionWriter writer = new RampMeteringSolutionWriterTXT();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writer.write_all_to_stream(stream,this);
        return stream.toString();
    }

}
