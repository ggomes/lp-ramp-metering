package ramp_metering;

import jaxb.Actuator;

public class RampMeteringLimit {
    public double min_rate; // flux in relation to max rate of link
    public double max_rate; // flux in relation to max rate of link
    public Actuator actuator;
}
