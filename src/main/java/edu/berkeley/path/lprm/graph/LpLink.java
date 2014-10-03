package edu.berkeley.path.lprm.graph;

/**
 * Created by gomes on 10/2/14.
 */
public class LpLink {

    public enum types {freeway,onramp,offramp,sink,other};

//    protected LpNetwork myNetwork;
    protected long id;
    protected types type;
    protected double length;
    protected double lanes;
    protected LpNode begin_node;
    protected LpNode end_node;
    protected boolean issource;

    public LpLink(edu.berkeley.path.beats.jaxb.Link link) {
        this.id = link.getId();
        String type_str = link.getLinkType().getName();
        if(type_str.compareToIgnoreCase("freeway")==0)
            this.type = types.freeway;
        else if(type_str.compareToIgnoreCase("on-ramp")==0)
            this.type = types.onramp;
        else if(type_str.compareToIgnoreCase("off-ramp")==0)
            this.type = types.offramp;
        else if(type_str.compareToIgnoreCase("sink")==0)
            this.type = types.sink;
        else
            this.type = types.other;
        this.length = link.getLength();
        this.lanes = link.getLanes();
    }

    protected void set_begin_node(LpNode node){
        begin_node = node;
    }

    protected void set_end_node(LpNode node){
        end_node = node;
    }

    public boolean isSource() {
        return issource;
    }

    public long getId(){
        return id;
    }
    public double getLength(){
        return length;
    }
    public double getLanes(){
        return lanes;
    }
    public LpNode getBegin(){
        return begin_node;
    }
    public LpNode getEnd(){
        return end_node;
    }

    public boolean isFreeway(){
        return type.compareTo(types.freeway)==0;
    }

    public boolean isOfframp(){
        return type.compareTo(types.offramp)==0;
    }

    public boolean isOnramp(){
        return type.compareTo(types.onramp)==0;
    }

    public boolean isSink(){
        return type.compareTo(types.sink)==0;
    }

}
