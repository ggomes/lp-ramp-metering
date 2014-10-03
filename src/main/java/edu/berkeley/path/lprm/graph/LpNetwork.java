package edu.berkeley.path.lprm.graph;

import edu.berkeley.path.beats.jaxb.Network;
import edu.berkeley.path.beats.jaxb.Link;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gomes on 10/2/14.
 */
public class LpNetwork {

    private ArrayList<LpLink> linklist = new ArrayList<LpLink>();
    private HashMap<Long,LpLink> links = new HashMap<Long,LpLink>();
    private HashMap<Long,LpNode> nodes = new HashMap<Long,LpNode>();

    public LpNetwork(Network jnet){

        long node_id;
        LpNode lpnode;

        for(Link jlink : jnet.getLinkList().getLink()){

            // add link
            LpLink lplink = new LpLink(jlink);
            links.put(jlink.getId(),lplink);
            linklist.add(lplink);

            // get/add begin node
            node_id = jlink.getBegin().getNodeId();
            if(nodes.containsKey(node_id)){
                lpnode = nodes.get(node_id);
            }
            else{
                lpnode = new LpNode(node_id);
                nodes.put(node_id,lpnode);
            }

            // connect the begin node
            lplink.set_begin_node(lpnode);
            lpnode.add_out_link(lplink);

            // get/add end node
            node_id = jlink.getEnd().getNodeId();
            if(nodes.containsKey(node_id)){
                lpnode = nodes.get(node_id);
            }
            else{
                lpnode = new LpNode(node_id);
                nodes.put(node_id,lpnode);
            }

            // connect the end node
            lplink.set_end_node(lpnode);
            lpnode.add_in_link(lplink);
        }

        // tag source links
        for(LpLink link : linklist)
            link.issource = link.begin_node.getnIn() ==0;

    }

    public ArrayList<LpLink> getLinks(){
        return linklist;
    }

}
