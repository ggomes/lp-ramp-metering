package edu.berkeley.path.lprm;

import edu.berkeley.path.beats.jaxb.Scenario;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by gomes on 6/5/14.
 */
final public class ObjectFactory {

    private ObjectFactory(){}

    public static edu.berkeley.path.beats.jaxb.Scenario getScenario(String configfilename) throws Exception {

        JAXBContext context;
        Unmarshaller u;

        // create unmarshaller .......................................................
        try {
            //Reset the classloader for main thread; need this if I want to run properly
            //with JAXB within MATLAB. (luis)
            Thread.currentThread().setContextClassLoader(ObjectFactory.class.getClassLoader());
            context = JAXBContext.newInstance("edu.berkeley.path.beats.jaxb");
            u = context.createUnmarshaller();
        } catch( JAXBException je ) {
            throw new Exception("Failed to create context for JAXB unmarshaller", je);
        }

        // schema assignment ..........................................................
        try{
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            ClassLoader classLoader = ObjectFactory.class.getClassLoader();
            Schema schema = factory.newSchema(classLoader.getResource("beats.xsd"));
            u.setSchema(schema);
        } catch(SAXException e){
            throw new Exception("Schema not found", e);
        }

        // process configuration file name ...........................................
        if(!configfilename.endsWith(".xml"))
            configfilename += ".xml";

        // read and return ...........................................................
        edu.berkeley.path.beats.jaxb.Scenario S; //new Scenario();
        try {
//            setObjectFactory(u, new JaxbObjectFactory());
            S = (Scenario) u.unmarshal( new FileInputStream(configfilename) );
        } catch( JAXBException je ) {
            throw new Exception("JAXB threw an exception when loading the configuration file", je);
        } catch (FileNotFoundException e) {
            throw new Exception("Configuration file not found. " + configfilename, e);
        }

        if(S==null)
            throw new Exception("Unknown load error");

        if(S.getSettings().getUnits().compareToIgnoreCase("si")!=0)
            throw new Exception("Scenario units must be SI");

        return S;
    }

    private static void setObjectFactory(Unmarshaller unmrsh, Object factory) throws PropertyException {
        final String classname = unmrsh.getClass().getName();
        String propnam = classname.startsWith("com.sun.xml.internal") ?//
                "com.sun.xml.internal.bind.ObjectFactory" ://
                "com.sun.xml.bind.ObjectFactory";
        unmrsh.setProperty(propnam, factory);
    }

}
