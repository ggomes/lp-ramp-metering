package lp;

import lp.problem.Linear;
import lp.problem.OptType;
import lp.problem.Relation;
import network.fwy.FwyNetwork;
import network.fwy.FwySegment;

/**
 * Created by gomes on 6/6/14.
 */
public class ProblemRampMetering extends lp.problem.Problem {

    protected int K;                // number of time steps (demand+cooldown)
    protected int Kcool;            // number of cooldown time steps
    protected double eta;           // objective = TVH - eta*TVM
    protected double sim_dt_in_seconds;

    public ProblemRampMetering(FwyNetwork fwy,int K_dem,int K_cool,double eta,double sim_dt_in_seconds){

        this.K = K_dem+K_cool;
        this.Kcool = K_cool;
        this.eta = eta;
        this.sim_dt_in_seconds = sim_dt_in_seconds;

        int i,k;
        double rhs;

        /* objective function:
           sum_[I][K] n[i][k] + sum_[Im][K] l[i][k] - eta sum_[I][K] f[i][k] - eta sum[Im][K] r[i][k]
         */
        Linear cost = new Linear();
        for(i=0;i<fwy.num_segments;i++){
            FwySegment seg = fwy.getSegments().get(i);
            for(k=0;k<K;k++){
                cost.add_coefficient( 1.0, getVar("n",i,k+1));
                cost.add_coefficient(-eta, getVar("f",i,k));
            }
            if(seg.is_metered)
                for(k=0;k<K;k++){
                    cost.add_coefficient( 1.0, getVar("l",i,k+1));
                    cost.add_coefficient(-eta, getVar("r",i,k));
                }
        }
        setObjective(cost, OptType.MIN);

        for(i=0;i<fwy.num_segments;i++){
            FwySegment seg = fwy.getSegments().get(i);

            double vf = seg.get_vf_vps()*sim_dt_in_seconds;
            double f_max = seg.get_fmax_vps()*sim_dt_in_seconds;
            double r_max = seg.get_rmax_vps()*sim_dt_in_seconds;

            for(k=0;k<K;k++){

                double time = k*sim_dt_in_seconds;
                double betabar = 1-seg.get_split_ratio(time);
                double d = seg.get_demand_in_vps(time)*sim_dt_in_seconds;
                double no = k==0? seg.no : 0;
                double lo = k==0? seg.lo : 0;

                // MAINLINE CONSERVATION .............................................

                Linear C1 = new Linear();

                // LHS
                // {always}    {k>0}     {i>0}   {metered}       {betabar[i][k]>0}
                // n[i][k+1] -n[i][k] -f[i-1][k]  -r[i][k] +inv(betabar[i][k])*f[i][k]
                C1.add_coefficient(+1d, getVar("n",i,k+1));
                if(k>0)
                    C1.add_coefficient(-1d, getVar("n",i,k));
                if(i>0)
                    C1.add_coefficient(-1d, getVar("f",i-1,k));
                if(seg.is_metered)
                    C1.add_coefficient(-1d, getVar("r",i,k));
                if(betabar!=0d)
                    C1.add_coefficient(+1/betabar, getVar("f",i,k));

                // relation
                C1.set_relation(Relation.EQ);

                // RHS
                // {k>0}  {k==0}
                //   0  + n[i][0]
                rhs = no;
                rhs += !seg.is_metered ? d : 0;
                C1.set_rhs(rhs);
                add_constraint(C1,"mlcons"+i+"_"+k);

                // MAINLINE FREEFLOW ...............................................
                Linear C3 = new Linear();

                // LHS
                // {always}           {k>0}                       {metered}
                // f[i][k] -betabar[i][k]*v[i]*n[i][k] - betabar[i][k]*v[i]*gamma*r[i][k]
                C3.add_coefficient(+1d, getVar("f",i,k));
                if(k>0 && betabar>0)
                    C3.add_coefficient(-betabar*vf, getVar("n",i,k));
                if(seg.is_metered && betabar>0)
                    C3.add_coefficient(-betabar*vf*fwy.gamma, getVar("r",i,k));

                // relation
                C3.set_relation(Relation.LEQ);

                // RHS
                //             {k==0}                      {!metered}
                // betabar[i][0]*v[i]*n[i][0] + betabar[i][k]*v[i]*gamma*d[i][k]
                rhs = betabar*vf*no;
                rhs += !seg.is_metered ? betabar*vf*fwy.gamma*d : 0;
                C3.set_rhs(rhs);

                add_constraint(C3,"orcons_"+i+"_"+k);

                // MAINLINE CONGESTION .................................................
                if(i<fwy.num_segments-1){
                    FwySegment next_seg = fwy.getSegments().get(i+1);

                    double next_w = next_seg.get_w_vps()*sim_dt_in_seconds;
                    double next_d = next_seg.get_demand_in_vps(time)*sim_dt_in_seconds;
                    double next_no = k==0? next_seg.no : 0;

                    Linear C4 = new Linear();

                    // LHS
                    // {always}       {k>0}                {metered}
                    // f[i][k] + w[i+1]*n[i+1][k] + w[i+1]*gamma*r[i+1][k]
                    C4.add_coefficient(+1d, getVar("f",i,k));
                    if(k>0)
                        C4.add_coefficient(next_w, getVar("n",i+1,k));
                    if(next_seg.is_metered)
                        C4.add_coefficient(next_w*fwy.gamma, getVar("r",i+1,k));

                    // relation
                    C4.set_relation(Relation.LEQ);

                    // RHS
                    //     {always}              {k=0}             {!metered}
                    // w[i+1]*njam[i+1] - w[i+1]*n[i+1][0] - w[i+1]*gamma*d[i+1][k]
                    rhs = next_w*next_seg.n_max;
                    rhs += -next_w*next_no;
                    rhs += !next_seg.is_metered ? -next_w*fwy.gamma*next_d : 0;
                    C4.set_rhs(rhs);

                    add_constraint(C4, "mlflw-cng_" + i + "_" + k);
                }

                // MAINLINE CAPACITY        f[i][k] <= f_max[i]
                add_bound(getVar("f", i, k), Relation.LEQ, f_max);

                if(seg.is_metered){

                    // ONRAMP CONSERVATION ......................................
                    Linear C2 = new Linear();

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

                    add_constraint(C2,"orcons_"+i+"_"+k);

                    // OR DEMAND ..................................................
                    Linear C5 = new Linear();

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

                    add_constraint(C5, "ordem_" + i + "_" + k);

                    // MAX METERING RATE: r[i][k] <= rmax[i] ............................
                    add_bound("max"+getVar("r",i,k),Relation.LEQ,r_max);

                    // ONRAMP FLOW NON-NEGATIVE: r[i][k] >= 0 ...........................
                    add_bound("min"+getVar("r",i,k),Relation.GEQ,0d);

                    // ONRAMP MAX QUEUE: l[i][k+1] <= lmax[i] .............................
                    if(!seg.l_max.isInfinite())
                        add_bound("max"+getVar("l",i,k+1),Relation.LEQ,seg.l_max);
                }
            }
        }
    }

    public static String getVar(String name,int index,int timestep){
        return name+"_"+index+"_"+timestep;
    }

}
