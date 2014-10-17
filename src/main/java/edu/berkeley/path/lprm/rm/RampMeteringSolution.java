package edu.berkeley.path.lprm.rm;


import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.fwy.FwySegment;
import edu.berkeley.path.lprm.lp.problem.PointValue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public final class RampMeteringSolution extends PointValue {

    public enum OutputFormat {text,matlab};

    protected ProblemRampMetering LP;
    protected FwyNetwork fwy;
    protected SegmentSolution [] Xopt;      // unpacked result in a convenient format
    protected CTMDistance ctm_distance;      // distance to mainline i,kth constraint

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

        // evaluate_state whether it is a valid ctm solution
        ctm_distance = evaluate_ctm_distance(LP,result);

        // cast the solution into segments
        Xopt = new SegmentSolution[I];

        int i,k;
        TVM = 0;
        TVH = 0;

        for(i=0;i<I;i++){

            FwySegment seg = fwy.get_segment(i);
            Xopt[i] = new SegmentSolution(seg,K);

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

    public Double get_max_ctm_distance(){
        return ctm_distance==null?null:ctm_distance.max_d;
    }

    public Double get_leftover_vehicles(){
        Double tv = 0d;
        for(SegmentSolution x : Xopt)
            tv += x.n[K] + (x.l!=null?x.l[K]:0d);
        return tv;
    }

//    public HashMap<String,Constraint.State> evaluate_constraints(double epsilon){
//        HashMap<String,Constraint.State> x = new HashMap<String,Constraint.State>();
//        x.putAll(LP.evaluate_constraint_state(this, epsilon));
//        x.putAll(LP.evaluate_lower_bounds(this,epsilon));
//        x.putAll(LP.evaluate_upper_bounds(this,epsilon));
//        return x;
//    }

    // computes metering rates
    public HashMap<Long,Double[]> get_metering_profiles_in_vps(){
        HashMap<Long,Double[]> profiles = new HashMap<Long,Double[]>();
        for(int i=0;i<fwy.get_num_segments();i++){
            FwySegment seg = fwy.get_segment(i);
            if(seg.is_metered())
                profiles.put( seg.get_on_ramp_link_id() , times(Xopt[i].r,1d/getSim_dt()) );
        }
        return profiles;
    }

    ////////////////////////////////////////////////////////
    // private statics
    ////////////////////////////////////////////////////////

    private static String getVar(String name,int seg_index,int timestep){
        return ProblemRampMetering.getVar(name,seg_index,timestep);
    }

    private static Double [] times(Double[] x, double a){
        Double [] r = x.clone();
        for(int i=0;i<r.length;i++)
            r[i] *= a;
        return r;
    }

    private static CTMDistance evaluate_ctm_distance(ProblemRampMetering LP,PointValue P){

        int I = LP.fwy.get_num_segments();
        int K = LP.K;

        String cnst_name;
        double eps = 1e-2;
        boolean feasible = true;
        Double slack_ff,slack_cp,slack_cg;
        CTMDistance ctm_distance = new CTMDistance(I,K);

        for (int i=0;i<I;i++){
            for (int k=0;k<K;k++){
                // ML freeflow
                cnst_name = ProblemRampMetering.getCnstr(ProblemRampMetering.CnstType.MLFLW_FF,i,k);
                slack_ff = LP.evaluate_constraint_slack(cnst_name,P);

                // ML capacity
                cnst_name = "UB_"+ ProblemRampMetering.getVar("f",i,k);
                slack_cp = LP.evaluate_constraint_slack(cnst_name,P);

                // ML congestion
                if(i<I-1){
                    cnst_name = ProblemRampMetering.getCnstr(ProblemRampMetering.CnstType.MLFLW_CNG,i,k);
                    slack_cg = LP.evaluate_constraint_slack(cnst_name,P);
                }
                else
                    slack_cg = Double.POSITIVE_INFINITY;

                if(slack_ff<-eps || slack_cp<-eps || slack_cg<-eps){
                    feasible = false;
                    break;
                }
               ctm_distance.add_value(i,k,Math.min(Math.min(slack_ff,slack_cp),slack_cg));
            }
        }
        return feasible ? ctm_distance : null;
    }

    ////////////////////////////////////////////////////////
    // internal class
    ////////////////////////////////////////////////////////

    // all units are normalized
    private class SegmentSolution {
        public Double [] n;     // [veh]
        public Double [] l;     // [veh]
        public Double [] f;     // [veh]
        public Double [] r;     // [veh]

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

    private static class CTMDistance {
        public Double [][] d;
        public Double max_d;
        public CTMDistance(int I,int K){
            d = new Double [I][K];
            max_d = Double.NEGATIVE_INFINITY;
        }
        public void add_value(int i,int k,double x){
            d[i][k] = x;
            if(x>max_d)
                max_d = x;
        }
        public void print(){
            int i,k;
            double eps = 1e-2;
            for(i=0;i<d.length;i++)
                for(k=0;k<d[i].length;k++)
                    if(d[i][k]>eps)
                        System.out.println(String.format("CTM_distance(%d,%d)=%f",i,k,d[i][k]));
        }
    }

    ////////////////////////////////////////////////////////
    // printing
    ////////////////////////////////////////////////////////

    public void print_ctm_distances(){
        this.ctm_distance.print();
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
