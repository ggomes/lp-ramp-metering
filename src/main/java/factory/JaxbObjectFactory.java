package factory;

import network.beats.Link;
import network.beats.Network;
import network.beats.Node;

/**
 * Created by gomes on 6/5/2014.
 */
public final class JaxbObjectFactory extends jaxb.ObjectFactory {

    @Override
    public jaxb.Link createLink() {
        return new Link();
    }

    @Override
    public jaxb.Network createNetwork() {
        return new Network();
    }

    @Override
    public jaxb.Node createNode() {
        return new Node();
    }
}
