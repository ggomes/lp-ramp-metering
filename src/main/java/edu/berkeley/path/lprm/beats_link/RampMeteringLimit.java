package edu.berkeley.path.lprm.beats_link;

import edu.berkeley.path.lprm.jaxb.Actuator;

public class RampMeteringLimit {
    public double min_rate; // flux in relation to max rate of link
    public double max_rate; // flux in relation to max rate of link
    public Actuator actuator;
}
