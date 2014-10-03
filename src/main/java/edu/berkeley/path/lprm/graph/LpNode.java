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

//    protected void populate(Network myNetwork) {
//
//        this.myNetwork = myNetwork;
//
//        nOut = 0;
//        if(getOutputs()!=null){
//            nOut = getOutputs().getOutput().size();
//            output_link = new Link[nOut];
//            for(int i=0;i<nOut;i++){
//                edu.berkeley.path.beats.jaxb.Output output = getOutputs().getOutput().get(i);
//                output_link[i] = myNetwork.getLinkWithId(output.getLinkId());
//            }
//        }
//
//        nIn = 0;
//        if(getInputs()!=null){
//            nIn = getInputs().getInput().size();
//            input_link = new Link[nIn];
//            for(int i=0;i<nIn;i++){
//                edu.berkeley.path.beats.jaxb.Input input = getInputs().getInput().get(i);
//                input_link[i] = myNetwork.getLinkWithId(input.getLinkId());
//            }
//        }
//
//        isTerminal = nOut==0 || nIn==0;
//    }
//
//    public boolean isTerminal() {
//        return isTerminal;
//    }
//
//    public Link[] getOutput_link() {
//        return output_link;
//    }
//
//    public Link[] getInput_link() {
//        return input_link;
//    }

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
