package edu.berkeley.path.lprm.rm;

import edu.berkeley.path.lprm.lp.problem.Constraint;
import edu.berkeley.path.lprm.lp.problem.Linear;
import edu.berkeley.path.lprm.lp.problem.OptType;
import edu.berkeley.path.lprm.lp.problem.Relation;
import edu.berkeley.path.lprm.fwy.FwyNetwork;
import edu.berkeley.path.lprm.fwy.FwySegment;

/**
 * Created by gomes on 6/6/14.
 */
public class ProblemRampMetering extends edu.berkeley.path.lprm.lp.problem.Problem {

    protected enum CnstType {
        MLCONS,
        MLFLW_FF,
        MLFLW_CNG,
//        MLFLW_CAP,
        ORCONS,
        ORFLW_DEM,
        ORMETER_MAX,
        ORQUEUE_MAX,
        ORFLW_POS
    }
    protected FwyNetwork fwy;
    protected int K_dem;
    protected int K_cool;
    protected int K;                // number of time steps (demand+cooldown)
    protected double t_start_cool;    // [sec]
    protected double eta;           // objective = TVH - eta*TVM
    protected double sim_dt_in_seconds;

    public ProblemRampMetering(FwyNetwork fwy,int K_dem,int K_cool,double eta,double sim_dt_in_seconds){

        this.fwy = fwy;
        this.K_dem = K_dem;
        this.K_cool = K_cool;
        this.K = K_dem+K_cool;
        this.t_start_cool = K_dem*sim_dt_in_seconds;
        this.eta = eta;
        this.sim_dt_in_seconds = sim_dt_in_seconds;

        int i,k;
        double rhs;

        // objective function: sum_[I][K] n[i][k] + sum_[Im][K] l[i][k] - eta sum_[I][K] f[i][k] - eta sum[Im][K] r[i][k]
        Linear cost = new Linear();
        for(i=0;i<fwy.get_num_segments();i++){
            FwySegment seg = fwy.get_segment(i);
            for(k=0;k<K;k++){
                cost.add_coefficient( 1.0, getVar("n",i,k+1));
                cost.add_coefficient(-eta, getVar("f",i,k));
            }
            if(seg.is_metered())
                for(k=0;k<K;k++){
                    cost.add_coefficient( 1.05, getVar("l",i,k+1));
                    cost.add_coefficient(-eta, getVar("r",i,k));
                }
        }
        setObjective(cost, OptType.MIN);

        for(i=0;i<fwy.get_num_segments();i++){
            FwySegment seg = fwy.get_segment(i);

            double vf = seg.get_vf_link_per_sec()*sim_dt_in_seconds;
            double f_max = seg.get_fmax_vps()*sim_dt_in_seconds;
            double r_max = seg.get_rmax_vps()*sim_dt_in_seconds;

            for(k=0;k<K;k++){

                double time = k*sim_dt_in_seconds;
                double betabar = 1-seg.get_split_ratio(time);
                double d = get_demand_for_seg_in_veh(seg,time);
                double no = k==0? seg.get_no() : 0;
                double lo = k==0? seg.get_lo() : 0;

                // MAINLINE CONSERVATION .............................................

                Constraint C1 = new Constraint();

                // LHS
                // {always}    {k>0}     {i>0}   {metered}       {betabar[i][k]>0}
                // n[i][k+1] -n[i][k] -f[i-1][k]  -r[i][k] +inv(betabar[i][k])*f[i][k]
                C1.add_coefficient(+1d, getVar("n",i,k+1));
                if(k>0)
                    C1.add_coefficient(-1d, getVar("n",i,k));
                if(i>0)
                    C1.add_coefficient(-1d, getVar("f",i-1,k));
                if(seg.is_metered())
                    C1.add_coefficient(-1d, getVar("r",i,k));
                if(betabar!=0d)
                    C1.add_coefficient(+1/betabar, getVar("f",i,k));

                // relation
                C1.set_relation(Relation.EQ);

                // RHS
                // {k>0}  {k==0}
                //   0  + n[i][0]
                rhs = no;
                rhs += !seg.is_metered() ? d : 0;
                C1.set_rhs(rhs);

                add_constraint(C1,getCnstr(CnstType.MLCONS,i,k));

                // MAINLINE FREEFLOW ...............................................
                Constraint C3 = new Constraint();

                // LHS
                // {always}           {k>0}                       {metered}
                // f[i][k] -betabar[i][k]*v[i]*n[i][k] - betabar[i][k]*v[i]*gamma*r[i][k]
                C3.add_coefficient(+1d, getVar("f",i,k));
                if(k>0 && betabar>0)
                    C3.add_coefficient(-betabar*vf, getVar("n",i,k));
                if(seg.is_metered() && betabar>0)
                    C3.add_coefficient(-betabar*vf*fwy.gamma(), getVar("r",i,k));

                // relation
                C3.set_relation(Relation.LEQ);

                // RHS
                //             {k==0}                      {!metered}
                // betabar[i][0]*v[i]*n[i][0] + betabar[i][k]*v[i]*gamma*d[i][k]
                rhs = betabar*vf*no;
                rhs += !seg.is_metered() ? betabar*vf*fwy.gamma()*d : 0;
                C3.set_rhs(rhs);

                add_constraint(C3,getCnstr(CnstType.MLFLW_FF,i,k));

                // MAINLINE CONGESTION .................................................
                if(i<fwy.get_num_segments()-1){
                    FwySegment next_seg = fwy.get_segment(i+1);;

                    double next_w = next_seg.get_w_link_per_sec()*sim_dt_in_seconds;
                    double next_d = get_demand_for_seg_in_veh(next_seg,time);
                    double next_no = k==0? next_seg.get_no() : 0;

                    Constraint C4 = new Constraint();

                    // LHS
                    // {always}       {k>0}                {metered}
                    // f[i][k] + w[i+1]*n[i+1][k] + w[i+1]*gamma*r[i+1][k]
                    C4.add_coefficient(+1d, getVar("f",i,k));
                    if(k>0)
                        C4.add_coefficient(next_w, getVar("n",i+1,k));
                    if(next_seg.is_metered())
                        C4.add_coefficient(next_w*fwy.gamma(), getVar("r",i+1,k));

                    // relation
                    C4.set_relation(Relation.LEQ);

                    // RHS
                    //     {always}              {k=0}             {!metered}
                    // w[i+1]*njam[i+1] - w[i+1]*n[i+1][0] - w[i+1]*gamma*d[i+1][k]
                    rhs = next_w*next_seg.get_nmax();
                    rhs += -next_w*next_no;
                    rhs += !next_seg.is_metered() ? -next_w*fwy.gamma()*next_d : 0;
                    C4.set_rhs(rhs);

                    add_constraint(C4,getCnstr(CnstType.MLFLW_CNG,i,k));
                }

                // MAINLINE CAPACITY        f[i][k] <= f_max[i]
                add_upper_bound(getVar("f", i, k),f_max);

                if(seg.is_metered()){

                    // ONRAMP CONSERVATION ......................................
                    Constraint C2 = new Constraint();

                    // LHS
                    //  {always}    {k>0}  {always}
                    // l[i][k+1]  -l[i][k] +r[i][k]
                    C2.add_coefficient(+1d, getVar("l",i,k+1));
                    if(k>0)
                        C2.add_coefficient(-1d, getVar("l",i,k));
                    C2.add_coefficient(+1d, getVar("r",i,k));

                    // relation
                    C2.set_relation(Relation.EQ);

                    // RHS
                    // {always}  {k==0}
                    // d[i][k] + l[i][0]
                    C2.set_rhs(d+lo);

                    add_constraint(C2,getCnstr(CnstType.ORCONS,i,k));

                    // OR DEMAND ..................................................
                    Constraint C5 = new Constraint();

                    // LHS
                    // {always}   {k>0}
                    // r[i][k] - l[i][k]
                    C5.add_coefficient(+1d, getVar("r",i,k));
                    if(k>0)
                        C5.add_coefficient(-1d, getVar("l",i,k));

                    // relation
                    C5.set_relation(Relation.LEQ);

                    // RHS
                    // {always}  {k==0}
                    // d[i][k] + l[i][0]
                    C5.set_rhs(d+lo);

                    add_constraint(C5,getCnstr(CnstType.ORFLW_DEM,i,k));

                    // MAX METERING RATE: r[i][k] <= rmax[i] ............................
                    if(!Double.isInfinite(r_max) && !Double.isNaN(r_max))
                        add_upper_bound(getVar("r", i, k),r_max);

                    // ONRAMP FLOW NON-NEGATIVE: r[i][k] >= 0 ...........................
                    add_lower_bound(getVar("r",i,k),0d);

                    // ONRAMP MAX QUEUE: l[i][k+1] <= lmax[i] .............................
                    if(!seg.get_lmax().isInfinite() && !seg.get_lmax().isNaN())
                        add_upper_bound(getVar("l",i,k+1),seg.get_lmax());
                }
            }
        }
    }

    public void set_rhs_from_fwy(FwyNetwork fwy){

        int i,k;
        double rhs;

        for(i=0;i<fwy.get_num_segments();i++){
            FwySegment seg = fwy.get_segment(i);

            double vf = seg.get_vf_link_per_sec()*sim_dt_in_seconds;

            for(k=0;k<K;k++){

                double time = k*sim_dt_in_seconds;
                double betabar = 1-seg.get_split_ratio(time);
                double d = get_demand_for_seg_in_veh(seg, time);
                double no = k==0? seg.get_no() : 0;
                double lo = k==0? seg.get_lo() : 0;

                // MAINLINE CONSERVATION ..............................
                rhs = no;
                rhs += !seg.is_metered() ? d : 0;
                set_constraint_rhs(getCnstr(CnstType.MLCONS,i,k),rhs);

                // MAINLINE FREEFLOW .................................
                rhs = betabar*vf*no;
                rhs += !seg.is_metered() ? betabar*vf*fwy.gamma()*d : 0;
                set_constraint_rhs(getCnstr(CnstType.MLFLW_FF,i,k),rhs);

                // MAINLINE CONGESTION ..............................
                if(i<fwy.get_num_segments()-1){
                    FwySegment next_seg = fwy.get_segment(i+1);

                    double next_w = next_seg.get_w_link_per_sec()*sim_dt_in_seconds;
                    double next_d = get_demand_for_seg_in_veh(next_seg,time);
                    double next_no = k==0? next_seg.get_no() : 0;

                    rhs = next_w*next_seg.get_nmax();
                    rhs += -next_w*next_no;
                    rhs += !next_seg.is_metered() ? -next_w*fwy.gamma()*next_d : 0;
                    set_constraint_rhs(getCnstr(CnstType.MLFLW_CNG,i,k),rhs);
                }

                if(seg.is_metered()){
                    // OR CONSERVATION .............................
                    set_constraint_rhs(getCnstr(CnstType.ORCONS,i,k),d+lo);

                    // OR DEMAND ...................................
                    set_constraint_rhs(getCnstr(CnstType.ORFLW_DEM,i,k),d+lo);
                }
            }
        }
    }

    public static String getVar(String name,int index,int timestep){
        return name+"["+index+"]["+timestep+"]";
    }

    public static String getCnstr(CnstType cnst,int i,int k){
        return cnst.toString() + "[" + i + "][" + k +"]";
    }

    private double get_demand_for_seg_in_veh(FwySegment seg,double time){
        return time>= t_start_cool ? 0d : seg.get_demand_in_vps(time)*sim_dt_in_seconds;
    }
}
