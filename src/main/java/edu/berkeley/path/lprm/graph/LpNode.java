package edu.berkeley.path.lprm.graph;

import edu.berkeley.path.beats.jaxb.Node;

import java.util.ArrayList;

/**
 * Created by gomes on 10/2/14.
 */
public class LpNode {

    protected long id;
    protected ArrayList<LpLink> inputs = new ArrayList<LpLink>();
    protected ArrayList<LpLink> outputs = new ArrayList<LpLink>();

    public LpNode(long id){
        this.id = id;
    }

    protected void add_out_link(LpLink link){
        outputs.add(link);
    }
    protected void add_in_link(LpLink link){
        inputs.add(link);
    }

    public int getnIn() {
        return inputs.size();
    }

    public int getnOut() {
        return outputs.size();
    }

    public long getId(){
        return id;
    }

    public ArrayList<LpLink> getInputs(){
        return inputs;
    }

    public LpLink getInput(int i){
        return inputs.get(i);
    }

    public ArrayList<LpLink> getOutputs(){
        return outputs;
    }

    public LpLink getOutput(int i){
        return outputs.get(i);
    }
}
