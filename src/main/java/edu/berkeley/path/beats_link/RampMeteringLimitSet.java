package edu.berkeley.path.beats_link;

import java.util.LinkedList;
import java.util.List;

public class RampMeteringLimitSet {
    public List<RampMeteringLimit> limits;
    public RampMeteringLimitSet() {
        limits = new LinkedList<RampMeteringLimit>();
    }
}