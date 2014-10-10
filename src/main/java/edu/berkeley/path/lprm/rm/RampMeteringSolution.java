package edu.berkeley.path.lprm.rm;

import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.fwy.FwySegment;
import edu.berkeley.path.lprm.lp.problem.PointValue;
import edu.berkeley.path.lprm.lp.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public final class RampMeteringSolution extends PointValue {

    public enum OutputFormat {text,matlab};

    protected ProblemRampMetering LP;
    protected FwyNetwork fwy;
    protected SegmentSolution [] Xopt;      // unpacked result in a convenient format

    protected boolean is_ctm;
    protected double TVH;
    protected double TVM;
    public int K;

    ////////////////////////////////////////////////////////
    // CONSTRUCTION
    ////////////////////////////////////////////////////////

    public RampMeteringSolution(ProblemRampMetering LP, FwyNetwork fwy, PointValue result){

        super(result);

        this.fwy = fwy;
        this.LP = LP;
        int I = fwy.get_num_segments();
        K = LP.K;
        double sim_dt = LP.sim_dt_in_seconds;

        // evaluate whether it is a valid ctm solution
        is_ctm = evaluate_is_ctm(LP,result);

        // cast the solution into segments
        Xopt = new SegmentSolution[I];

        int i,k;
        int actuatedORCounter = 0;
        int frCounter = 0;
        TVM = 0;
        TVH = 0;

        for(i=0;i<I;i++){

            FwySegment seg = fwy.get_segment(i);
            Xopt[i] = new SegmentSolution(seg,K);

//            // make local copy of link ids (is this necessary?)
//            mainLineIDs[i] = seg.get_main_line_link_id();
//            if (seg.is_metered())
//                actuatedOnRampIDs[actuatedORCounter++] = seg.get_on_ramp_link_id(); // get_actuated_on_ramp_id();
//            if (seg.has_offramp())
//                offRampIDs[frCounter++] = seg.get_off_ramp_id();

            // store state in Xopt
            Xopt[i].n[0] = seg.get_no();
            for(k=0;k<K;k++){
                Xopt[i].n[k+1] = result.get(getVar("n",i,k+1));
                Xopt[i].f[k] = result.get(getVar("f", i, k));
            }
            if(seg.is_metered()){
                Xopt[i].l[0] = seg.get_lo();
                for(k=0;k<K;k++){
                    Xopt[i].l[k+1] = result.get(getVar("l",i,k+1));
                    Xopt[i].r[k] = result.get(getVar("r", i, k));
                }
            }

            // add terms to TVH and TVM
            for (k=0;k<K;k++) {
                double d = seg.get_demand_in_vps(k)*sim_dt;
                TVH += result.get(getVar("n",i,k+1));
                if (seg.has_offramp())
                    TVM += result.get(getVar("f", i, k)) / (1 - seg.get_split_ratio(k));
                else
                    TVM += result.get(getVar("f", i, k));
                if (seg.has_onramp()){
                    TVM += (seg.is_metered()? result.get(getVar("r", i, k)) : d);
                    TVH += (seg.is_metered()? result.get(getVar("l",i,k+1)) : 0);
                }
            }

        }
    }

    ////////////////////////////////////////////////////////
    // public getters and setters
    ////////////////////////////////////////////////////////

    // Link ids ....................

    public ArrayList<Long> get_mainline_ids(){
        return fwy.get_mainline_ids();
    }

    public ArrayList<Long> get_offramp_ids(){
        return fwy.get_offramp_ids();
    }

    public ArrayList<Long> get_metered_onramp_ids(){
        return fwy.get_metered_onramp_ids();
    }

    // performance ....................

    public double getTVH(){
        return  TVH;
    }

    public double getTVM(){
        return TVM;
    }

    // other ....................

    public ProblemRampMetering get_lp(){
        return LP;
    }

    public FwyNetwork get_fwy(){
        return fwy;
    }

    public double getSim_dt(){
        return LP.sim_dt_in_seconds;
    }

    public boolean is_ctm(){
        return is_ctm;
    }

    public HashMap<String,Problem.ConstraintState> evaluate_constraints(double epsilon){
        HashMap<String,Problem.ConstraintState> x = new HashMap<String,Problem.ConstraintState>();
        x.putAll(LP.evaluate_constraints(this,epsilon));
        x.putAll(LP.evaluate_lower_bounds(this,epsilon));
        x.putAll(LP.evaluate_upper_bounds(this,epsilon));
        return x;
    }


    ////////////////////////////////////////////////////////
    // private statics
    ////////////////////////////////////////////////////////

    private static String getVar(String name,int seg_index,int timestep){
        return ProblemRampMetering.getVar(name,seg_index,timestep);
    }

    private static boolean evaluate_is_ctm(ProblemRampMetering LP,PointValue result){

        int I = LP.fwy.get_num_segments();
        int K = LP.K;

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
                String or_positive_key = "r";
                String index_string = "[" + Integer.toString(i)+ "]" + "[" + Integer.toString(k) + "]";

                ml_free_flow_key = ml_free_flow_key.concat(index_string);
                ml_cong_flow_key = ml_cong_flow_key.concat(index_string);
                ml_capacity_flow_key = ml_capacity_flow_key.concat(index_string);
                ml_cons_key = ml_cons_key.concat(index_string);

                or_positive_key = or_positive_key.concat(index_string);
                ml_not_CTM_key = ml_not_CTM_key.concat(index_string);

                Problem.ConstraintState ml_FFL_const = constraint_evaluation.get(ml_free_flow_key);
                Problem.ConstraintState ml_CNG_const = constraint_evaluation.get(ml_cong_flow_key);
                Problem.ConstraintState ml_cap_const = upper_bounds_evaluation.get(ml_capacity_flow_key);
                Problem.ConstraintState ml_cons_const = constraint_evaluation.get(ml_cons_key);
                Problem.ConstraintState or_positive_const = lower_bounds_evaluation.get(or_positive_key);

                if (ml_FFL_const == Problem.ConstraintState.active)
                    CTM_behavior.put(ml_free_flow_key,ml_FFL_const);
                if (ml_CNG_const == Problem.ConstraintState.active)
                    CTM_behavior.put(ml_cong_flow_key, Problem.ConstraintState.active);
                if (ml_cap_const == Problem.ConstraintState.active)
                    CTM_behavior.put(ml_capacity_flow_key, Problem.ConstraintState.active);

                if (ml_FFL_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_free_flow_key, Problem.ConstraintState.violated);

                if (ml_CNG_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_cong_flow_key, Problem.ConstraintState.violated);

                if (ml_cap_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_capacity_flow_key, Problem.ConstraintState.violated);

                if (ml_cons_const == Problem.ConstraintState.violated)
                    cnst_violated.put(ml_cons_key, Problem.ConstraintState.violated);

                if (or_positive_const == Problem.ConstraintState.violated)
                    cnst_violated.put(or_positive_key, Problem.ConstraintState.violated);


                boolean not_ctm = ml_FFL_const != Problem.ConstraintState.active && ml_CNG_const != Problem.ConstraintState.active &&
                            ml_cap_const != Problem.ConstraintState.active;

                if (not_ctm)
                    {
                       if (ml_FFL_const == Problem.ConstraintState.violated || ml_CNG_const == Problem.ConstraintState.violated ||
                                ml_cap_const == Problem.ConstraintState.violated)
                            not_CTM_behavior.put(ml_not_CTM_key, Problem.ConstraintState.violated);
                       else
                            not_CTM_behavior.put(ml_not_CTM_key, Problem.ConstraintState.inactive);
                    }

            }
        }

        return not_CTM_behavior.isEmpty();
    }

    ////////////////////////////////////////////////////////
    // internal class
    ////////////////////////////////////////////////////////

    public class SegmentSolution {
        protected Double [] n;
        protected Double [] l;
        protected Double [] f;
        protected Double [] r;

        public SegmentSolution(FwySegment fseg,int K){
            n = new Double[K+1];
            f = new Double[K];
            if(fseg.is_metered()){
                l = new Double[K+1];
                r = new Double[K];
            }
        }
        public Double [] get(String name){
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

    public ArrayList<Double[]> get_matrix(String name){
        ArrayList<Double[]> X = new ArrayList<Double[]>();
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
