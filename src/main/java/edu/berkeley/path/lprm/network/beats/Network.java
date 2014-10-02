
package edu.berkeley.path.lprm.network.beats;

public class Network extends edu.berkeley.path.lprm.jaxb.Network {

	public void populate() {

		// nodes
		for (edu.berkeley.path.lprm.jaxb.Node node : getNodeList().getNode())
			((Node) node).populate(this);

		// links
		for (edu.berkeley.path.lprm.jaxb.Link link : getLinkList().getLink())
			((Link) link).populate(this);
		
	}

	public Link getLinkWithId(long id){
		if(getLinkList()==null)
			return null;
		for(edu.berkeley.path.lprm.jaxb.Link link : getLinkList().getLink()){
			if(link.getId()==id)
				return (Link) link;
		}
		return null;
	}

	public Node getNodeWithId(long id){
		if(getNodeList()==null)
			return null;
		for(edu.berkeley.path.lprm.jaxb.Node node : getNodeList().getNode()){
			if(node.getId()==id)
				return (Node) node;
		}
		return null;
	}

}
