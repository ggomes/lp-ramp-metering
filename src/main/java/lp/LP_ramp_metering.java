package lp;

import jaxb.*;
import network.beats.Network;
import network.fwy.FwyNetwork;
import network.fwy.FwySegment;
import lp.problem.*;

public class LP_ramp_metering {

    protected FwyNetwork fwy;
    protected int I;                      // number of segments
    protected double gamma = 1d;          // merge coefficient

    protected Problem LP;
    protected int K;                      // number of time steps (demand+cooldown)
    protected int Kcool;                  // number of cooldown time steps
    protected double eta = 1.0;           // objective = TVH - eta*TVM

    protected double sim_dt_in_seconds;   // time step in seconds

    //protected Linear objective = new Linear();
//    protected boolean is_valid;
//    protected String validation_message;

    public void set_fwy(Network net, FundamentalDiagramSet fd_set, ActuatorSet actuator_set) throws Exception{

        int i,k;
        double rhs;

        // Make the freeway structure
        fwy = new FwyNetwork(net,fd_set,actuator_set,sim_dt_in_seconds);
        this.I = fwy.getSegments().size();

        // Make the LP
        LP = new Problem();

        /* objective function:
           sum_[I][K] n[i][k] + sum_[Im][K] l[i][k] - eta sum_[I][K] f[i][k] - eta sum[Im][K] r[i][k]
         */
        Linear cost = new Linear();
        for(i=0;i<I;i++){
            FwySegment seg = fwy.getSegments().get(i);
            for(k=0;k<K;k++){
                cost.add_coefficient(1.0, getVar("n",i,k + 1));
                cost.add_coefficient(-eta, getVar("f",i,k));
            }
            if(seg.is_metered)
                for(k=0;k<K;k++){
                    cost.add_coefficient(1.0, getVar("l",i,k + 1));
                    cost.add_coefficient(-eta, getVar("r",i,k));
                }
        }
        LP.setObjective(cost,OptType.MIN);


        for(i=0;i<I;i++){
            FwySegment seg = fwy.getSegments().get(i);
            for(k=0;k<K;k++){

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
                if(seg.betabar(k)!=0d)
                    C1.add_coefficient(+1 / seg.betabar(k), getVar("f",i,k));

                // relation
                C1.set_relation(Relation.EQ);

                // RHS
                // {k>0}  {k==0}
                //   0  + n[i][0]
                rhs = k==0 ? seg.no : 0;
                rhs += !seg.is_metered ? seg.d(k) : 0;
                C1.set_rhs(rhs);
                LP.add_constraint(C1,"mlcons"+i+"_"+k);


                // MAINLINE FREEFLOW ...............................................
                Linear C3 = new Linear();

                // LHS
                // for each i in 0...I-1, k in 0...K-1
                // {always}           {k>0}                       {metered}
                // f[i][k] -betabar[i][k]*v[i]*n[i][k] - betabar[i][k]*v[i]*gamma*r[i][k]
                C3.add_coefficient(+1d, getVar("f",i,k));
                if(k>0 && seg.betabar(k)>0)
                    C3.add_coefficient(-seg.betabar(k) * seg.vf, getVar("n",i,k));
                if(seg.is_metered && seg.betabar(k)>0)
                    C3.add_coefficient(-seg.betabar(k) * seg.vf * gamma, getVar("r",i,k));

                // RHS
                //                {k==0}                      {!metered}
                // <= betabar[i][0]*v[i]*n[i][0] + betabar[i][k]*v[i]*gamma*d[i][k]
                rhs = k==0 ? seg.betabar(0)*seg.vf*seg.no : 0;
                rhs += !seg.is_metered ? seg.betabar(k)*seg.vf*gamma*seg.d(k) : 0;
                LP.add(C3, "<=", rhs);


                // MAINLINE CONGESTION .................................................
                if(i<I-1){
                    FwySegment next_seg = fwy.getSegments().get(i+1);

                    Linear C = new Linear();

                    // LHS
                    // {always}       {k>0}                {metered}
                    // f[i][k] + w[i+1]*n[i+1][k] + w[i+1]*gamma*r[i+1][k]
                    C.add_coefficient(+1d, getVar("f",i,k));
                    if(k>0)
                        C.add_coefficient(next_seg.w, getVar("n",i+1,k));
                    if(next_seg.is_metered)
                        C.add_coefficient(next_seg.w * gamma, getVar("r",i+1,k));
                    LP.add_constraint(C, "mlflw-cng_" + i + "_" + k);

                    // RHS
                    // {always}              {k=0}             {!metered}
                    // LHS <= w[i+1]*njam[i+1] - w[i+1]*n[i+1][0] - w[i+1]*gamma*d[i+1][k]
                    rhs = next_seg.w*next_seg.n_max;
                    rhs += k==0 ? -next_seg.w*next_seg.no : 0;
                    rhs += !next_seg.is_metered ? -gamma*next_seg.w*next_seg.d(k) : 0;
                    LP.add(seg.MLcng.get(k), "<=", rhs);
                }

                // MAINLINE CAPACITY        f[i][k] <= f_max[i]
                LP.setVarUpperBound(getVar("f",i,k),seg.f_max);

                if(seg.is_metered){

                    // ONRAMP CONSERVATION ......................................
                    Linear C2 = new Linear();

                    // LHS
                    // {always}    {k>0}  {always}
                    // l[i][k+1]  -l[i][k] +r[i][k]
                    C2.add_coefficient(+1d, getVar("l",i,k+1));
                    if(k>0)
                        C2.add_coefficient(-1d, getVar("l",i,k));
                    C2.add_coefficient(+1d, getVar("r",i,k));
                    LP.add_constraint(C2, "orcons_" + i + "_" + k);

                    // RHS
                    // {always}  {k==0}
                    // d[i][k] + l[i][0]
                    rhs = seg.d(k);
                    rhs += k==0 ? seg.lo : 0;
                    LP.add_constraint(seg.ORcons.get(k), "=", rhs);

                    // OR DEMAND ..................................................
                    Linear C = new Linear();

                    // LHS
                    // {always}   {k>0}
                    // r[i][k] - l[i][k]
                    C.add_coefficient(+1d, getVar("r",i,k));
                    if(k>0)
                        C.add_coefficient(-1d, getVar("l",i,k));
                    LP.add_constraint(C, "ordem_" + i + "_" + k);

                    // RHS
                    // {always}
                    // <= d[i][k]
                    rhs = k==0 ? seg.lo : 0;
                    rhs += seg.d(k);
                    LP.add(seg.ORdem.get(k), "<=", rhs);

                    // MAX METERING RATE        r[i][k] <= rmax[i]
                    LP.add_bound("max"+getVar("r",i,k),Relation.LEQ,seg.r_max);

                    // ONRAMP FLOW NON-NEGATIVE     r[i][k] >= 0
                    LP.add_bound("min"+getVar("r",i,k),Relation.GEQ,0d);

                    // ONRAMP MAX QUEUE         l[i][k] <= lmax[i]
                    if(!seg.l_max.isInfinite())
                        LP.add_bound("max"+getVar("l",i,k+1),Relation.LEQ,seg.l_max);
                }
            }
        }
    }

    public void set_inital_condition(InitialDensitySet ic){
        fwy.set_ic(ic);
    }

    public void set_boundary_conditions(DemandSet demand_set,SplitRatioSet split_ratios) throws Exception{
        fwy.set_demands(demand_set,sim_dt_in_seconds,K,Kcool);
        fwy.set_split_ratios(split_ratios,sim_dt_in_seconds,K,Kcool);
    }

    public void set_parameters(int K_dem,int K_cool, double sim_dt_in_seconds){
        this.K = K_dem+K_cool;
        this.Kcool = K_cool;
        this.sim_dt_in_seconds = sim_dt_in_seconds;
    }

    public LP_solution solve() throws Exception {
        return new LP_solution(LP,fwy,K);
    }


    public static String getVar(String name,int index,int timestep){
        return name+"_"+index+"_"+timestep;
    }
}