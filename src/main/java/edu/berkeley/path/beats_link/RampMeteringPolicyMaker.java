package edu.berkeley.path.beats_link;

import jaxb.*;
import jaxb.Network;

public interface RampMeteringPolicyMaker {
    // dt should be same across all passed in objects (universal simulation dt)
    RampMeteringPolicySet givePolicy(Network net,
                                     FundamentalDiagramSet fd,
                                     DemandSet demand,
                                     SplitRatioSet splitRatios,
                                     InitialDensitySet ics,
                                     RampMeteringLimitSet control,
                                     Double dt);
}
