package edu.berkeley.path.beats_link;

import jaxb.Actuator;

import java.util.LinkedList;
import java.util.List;

public class RampMeteringPolicyProfile {
    public Actuator ramp_meter;
    public List<Double> metering_rate_vph;

    public RampMeteringPolicyProfile() {
        metering_rate_vph = new LinkedList<Double>();
    }

    public void print() {
        System.out.println("Actuator id: " + ramp_meter.getId());
        for (Double d : metering_rate_vph)
            System.out.print(d + ",");
        System.out.println();
    }
}