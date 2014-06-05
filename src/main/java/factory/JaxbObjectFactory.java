package factory;

/**
 * Created by gomes on 6/5/2014.
 */
public final class JaxbObjectFactory extends jaxb.ObjectFactory {

    @Override
    public jaxb.Link createLink() {
        return new beats.Link();
    }

    @Override
    public jaxb.Network createNetwork() {
        return new beats.Network();
    }

    @Override
    public jaxb.Node createNode() {
        return new beats.Node();
    }
}
