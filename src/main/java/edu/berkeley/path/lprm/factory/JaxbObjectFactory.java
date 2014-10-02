package edu.berkeley.path.lprm.factory;

import edu.berkeley.path.lprm.network.beats.Link;
import edu.berkeley.path.lprm.network.beats.Network;
import edu.berkeley.path.lprm.network.beats.Node;

/**
 * Created by gomes on 6/5/2014.
 */
public final class JaxbObjectFactory extends edu.berkeley.path.lprm.jaxb.ObjectFactory {

    @Override
    public edu.berkeley.path.lprm.jaxb.Link createLink() {
        return new Link();
    }

    @Override
    public edu.berkeley.path.lprm.jaxb.Network createNetwork() {
        return new Network();
    }

    @Override
    public edu.berkeley.path.lprm.jaxb.Node createNode() {
        return new Node();
    }
}
