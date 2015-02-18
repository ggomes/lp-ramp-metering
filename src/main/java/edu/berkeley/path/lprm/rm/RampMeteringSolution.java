package edu.berkeley.path.lprm.rm;


import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.fwy.FwySegment;
import edu.berkeley.path.lprm.fwy.FwyStateTrajectory;
import edu.berkeley.path.lprm.lp.problem.Constraint;
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

    public int K;

    ////////////////////////////////////////////////////////
    // CONSTRUCTION
    ////////////////////////////////////////////////////////

    public RampMeteringSolution(ProblemRampMetering LP, FwyNetwork fwy, FwyStateTrajectory trj){
        this(LP,fwy,new PointValue(trj));
    }

    public RampMeteringSolution(ProblemRampMetering LP, FwyNetwork fwy, PointValue result){

        super(result);

        this.fwy = fwy;
        this.LP = LP;
        int I = fwy.get_num_segments();
        K = LP.K;

        // cast the solution into segments
        Xopt = new SegmentSolution[I];

        int i,k;

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

        }

        // evaluate_state whether it is a valid ctm solution
        ctm_distance = evaluate_ctm_distance(LP);
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

    public double get_ml_tvh() {
        double x = 0d;
        for (SegmentSolution ss : Xopt)
            x += sum(ss.n);
        return x * getSim_dt() / 3600d;
    }

    public double get_or_tvh() {
        double x = 0d;
        for (SegmentSolution ss : Xopt)
            x += sum(ss.l);
        return x * getSim_dt() / 3600d;
    }

    public double get_tvh() {
        double x = 0d;
        for (SegmentSolution ss : Xopt)
            x += sum(ss.n) + sum(ss.l);
        return x * getSim_dt() / 3600d;
    }

    public double get_tvm() {
        double x = 0d;
        for (SegmentSolution ss : Xopt)
            x += sum(ss.f) + sum(ss.r);
        return x * getSim_dt() / 3600d;
    }

    public double get_cost(){
        return LP.evaluate_cost(this);
    }

    private static double sum(double [] x){
        double s = 0d;
        for(int i=0;i<x.length;i++)
            s += x[i];
        return s;
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

    public HashMap<Long,double[]> get_ramp_flow_in_veh(){
        HashMap<Long,double[]> profiles = new HashMap<Long,double[]>();
        for(int i=0;i<fwy.get_num_segments();i++){
            FwySegment seg = fwy.get_segment(i);
            if(seg.is_metered())
                profiles.put( seg.get_on_ramp_link_id() , Xopt[i].r );
        }
        return profiles;
    }

    ////////////////////////////////////////////////////////
    // private statics
    ////////////////////////////////////////////////////////

    private static String getVar(String name,int seg_index,int timestep){
        return ProblemRampMetering.getVar(name,seg_index,timestep);
    }

    private static double [] times(double[] x, double a){
        double [] r = x.clone();
        for(int i=0;i<r.length;i++)
            r[i] *= a;
        return r;
    }

    private CTMDistance evaluate_ctm_distance(ProblemRampMetering LP){

        int I = LP.fwy.get_num_segments();
        int K = LP.K;

        String cnstFF_name,cnstCP_name,cnstCG_name;
        double eps = 1e-2;
        boolean feasible = true;
        Double slack_ff,slack_cp,slack_cg;
        CTMDistance ctm_distance = new CTMDistance(I,K);

        for (int i=0;i<I;i++){
            for (int k=0;k<K;k++){

                double f = Xopt[i].f[k];

                // ML freeflow
                cnstFF_name = ProblemRampMetering.getCnstr(ProblemRampMetering.CnstType.MLFLW_FF, i, k);
                slack_ff = LP.evaluate_constraint_slack_veh(cnstFF_name,this);

                // ML capacity
                cnstCP_name = "UB_"+ ProblemRampMetering.getVar("f",i,k);
                slack_cp = LP.evaluate_constraint_slack_veh(cnstCP_name,this);

                // ML congestion
                if(i<I-1){
                    cnstCG_name = ProblemRampMetering.getCnstr(ProblemRampMetering.CnstType.MLFLW_CNG, i, k);
                    slack_cg = LP.evaluate_constraint_slack_veh(cnstCG_name,this);
                }
                else {
                    slack_cg = Double.POSITIVE_INFINITY;
                }

                if(slack_ff<-eps || slack_cp<-eps || slack_cg<-eps){
                    feasible = false;
                    break;
                }


                double d = Math.min(Math.min(slack_ff, slack_cp), slack_cg);
                ctm_distance.add_value(i,k,d);

            }
        }
        return feasible ? ctm_distance : null;
    }

    ////////////////////////////////////////////////////////
    // internal class
    ////////////////////////////////////////////////////////

    // all units are normalized
    private class SegmentSolution {
        public double [] n;     // [veh]
        public double [] l;     // [veh]
        public double [] f;     // [veh]
        public double [] r;     // [veh]

        public SegmentSolution(FwySegment fseg,int K){
            n = new double[K+1];
            f = new double[K];
            l = new double[K+1];
            r = new double[K];
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
