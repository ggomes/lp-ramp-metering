package edu.berkeley.path.beats_link;

import java.util.LinkedList;
import java.util.List;

public class RampMeteringPolicySet {
    public List<RampMeteringPolicyProfile> profiles;

    public RampMeteringPolicySet() {
        profiles = new LinkedList<RampMeteringPolicyProfile>();
    }

    public void print() {
        for (RampMeteringPolicyProfile profile : profiles) {
            profile.print();
        }
    }
}
