package edu.berkeley.path.lprm.rm;

import edu.berkeley.path.lprm.lp.problem.PointValue;
import edu.berkeley.path.lprm.lp.problem.Problem;
import edu.berkeley.path.lprm.lp.solver.ApacheSolver;
import edu.berkeley.path.lprm.lp.solver.LpSolveSolver;
import edu.berkeley.path.lprm.lp.solver.Solver;
import edu.berkeley.path.lprm.lp.solver.SolverType;
import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.fwy.FwySegment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public final class RampMeteringSolution {

    public enum OutputFormat {text,matlab};

    protected PointValue result;
    protected SegmentSolution [] Xopt;      // unpacked result in a convenient format

    protected boolean is_ctm;

    protected double[] mainLineIDs;
    protected double[] actuatedOnRampIDs;
    protected double[] offRampIDs;
    protected double TVH;
    protected double TVM;

    public int K;
    protected int I;
    protected double sim_dt;

    ////////////////////////////////////////////////////////
    // CONSTRUCTION
    ////////////////////////////////////////////////////////

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
//            case LPSOLVE:
//                solver = new LpSolveSolver();
//                break;
//            case GUROBI:
//                solver = new GurobiSolver();
//                break;
        }

        result = solver.solve(LP);

        is_ctm = evaluate_is_ctm(LP,result,I,K);

        Xopt = new SegmentSolution[I];

        int i,k;
        int actuatedORCounter = 0;
        int frCounter = 0;
        TVM = 0;
        TVH = 0;

        for(i=0;i<I;i++){

            FwySegment seg = fwy.get_segment(i);


            Xopt[i] = new SegmentSolution(seg,K);

            mainLineIDs[i] = seg.get_main_line_link_id();

            if (seg.is_metered)
                actuatedOnRampIDs[actuatedORCounter++] = seg.get_on_ramp_link_id(); // get_actuated_on_ramp_id();

            if (seg.has_off_ramp)
                offRampIDs[frCounter++] = seg.get_off_ramp_id();

            Xopt[i].n[0] = seg.get_no();
            for(k=0;k<K;k++){
                Xopt[i].n[k+1] = result.get(getVar("n",i,k+1));
                Xopt[i].f[k] = result.get(getVar("f", i, k));
            }

            if(seg.is_metered){
                Xopt[i].l[0] = seg.get_lo();
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
    }

    ////////////////////////////////////////////////////////
    // public getters and setters
    ////////////////////////////////////////////////////////

    // Link ids ....................

    public double[] get_ml_ids(){
        return mainLineIDs;
    }

    public double[] get_actuated_or_ids(){
        return actuatedOnRampIDs;
    }

    public double[] get_fr_ids(){
        return offRampIDs;
    }

    // performance ....................

//    public double getTVH(){
//        return  TVM;
//    }
//
//    public double getTVM(){
//        return TVH;
//    }

    // other ....................

    public double getSim_dt(){
        return sim_dt;
    }

//    public double get_cost(){
//        return result.cost;
//    }
//
//    public PointValue get_PointValue(){
//        return result;
//    }


//    public boolean is_CTM(){
//        return is_ctm;
//    }

    ////////////////////////////////////////////////////////
    // private statics
    ////////////////////////////////////////////////////////

    private static String getVar(String name,int seg_index,int timestep){
        return ProblemRampMetering.getVar(name,seg_index,timestep);
    }

    private static boolean evaluate_is_ctm(ProblemRampMetering LP,PointValue result,int I,int K){

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

        return not_CTM_behavior.isEmpty();
    }

    ////////////////////////////////////////////////////////
    // internal class
    ////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////
    // printing
    ////////////////////////////////////////////////////////

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

    public ArrayList<double[]> get_matrix(String name){
        ArrayList<double[]> X = new ArrayList<double[]>();
        for(SegmentSolution segsol : Xopt)
            X.add(segsol.get(name));
        return X;
    }

    @Override
    public String toString() {
        RampMeteringSolutionWriter writer = new RampMeteringSolutionWriterTXT();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writer.write_all_to_stream(stream,this);
        return stream.toString();
    }

}
