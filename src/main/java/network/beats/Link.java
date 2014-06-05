
package network.beats;

public class Link extends jaxb.Link {

    protected Network myNetwork;
    protected Node begin_node;
    protected Node end_node;
    protected boolean issource; 						// [boolean]

    public Link() {}

    protected void populate(Network myNetwork) {

        this.myNetwork = myNetwork;

        // make network connections
        begin_node = myNetwork.getNodeWithId(getBegin().getNodeId());
        end_node = myNetwork.getNodeWithId(getEnd().getNodeId());

        // nodes must populate before links
        if(begin_node!=null)
            issource = begin_node.isTerminal();

    }

    public Node getBegin_node() {
        return begin_node;
    }

    public Node getEnd_node() {
        return end_node;
    }

    public boolean isSource() {
        return issource;
    }
}
