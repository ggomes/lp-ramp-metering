
package edu.berkeley.path.lprm.network.beats;

public class Node extends edu.berkeley.path.lprm.jaxb.Node {

	protected Network myNetwork;
	protected int nIn;
	protected int nOut;
	protected Link [] output_link;
	protected Link [] input_link;
    protected boolean isTerminal;

	public Node(){}

	protected void populate(Network myNetwork) {

		this.myNetwork = myNetwork;
		
		nOut = 0;
		if(getOutputs()!=null){
			nOut = getOutputs().getOutput().size();
			output_link = new Link[nOut];
			for(int i=0;i<nOut;i++){
                edu.berkeley.path.lprm.jaxb.Output output = getOutputs().getOutput().get(i);
				output_link[i] = myNetwork.getLinkWithId(output.getLinkId());
			}
		}

		nIn = 0;
		if(getInputs()!=null){
			nIn = getInputs().getInput().size();
			input_link = new Link[nIn];
			for(int i=0;i<nIn;i++){
                edu.berkeley.path.lprm.jaxb.Input input = getInputs().getInput().get(i);
				input_link[i] = myNetwork.getLinkWithId(input.getLinkId());
			}
		}

        isTerminal = nOut==0 || nIn==0;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public Link[] getOutput_link() {
		return output_link;
	}

	public Link[] getInput_link() {
		return input_link;
	}
	
	public int getnIn() {
		return nIn;
	}

	public int getnOut() {
		return nOut;
	}

}
