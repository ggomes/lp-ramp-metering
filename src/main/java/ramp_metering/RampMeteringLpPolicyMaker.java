package ramp_metering;

import jaxb.*;

/**
 * Created by gomes on 6/5/14.
 */
public class RampMeteringLpPolicyMaker implements RampMeteringPolicyMaker {

    @Override
    public RampMeteringPolicySet givePolicy(Network net, FundamentalDiagramSet fd, DemandSet demand, SplitRatioSet splitRatios, InitialDensitySet ics, RampMeteringLimitSet control, Double dt) {
        return null;
    }

}
