package au.org.ands.vocabs.toolkit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class providing access to toolkit properties. */
public final class ToolkitProperties {

    /** Base name of the properties file. */
    private static final String PROPS_FILE = "toolkit.properties";

    /** Properties object. */
    private static Properties props;

    /** Logger. */
    private static Logger logger;

    /** This is a utility class. No instantiation. */
    private ToolkitProperties() {
    }

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

    /** Get the toolkit properties.
     * @return the properties
     */
    public static Properties getProperties() {
        if (props == null) {
            initProperties();
        }
        return props;
    }

    /** Get a toolkit property.
     * @param propName the property name
     * @return the properties
     */
    public static String getProperty(final String propName) {
        if (props == null) {
            initProperties();
        }
        return props.getProperty(propName);
    }

    /** Initialize the toolkit properties.
     */
    private static void initProperties() {
        props = new Properties();
        InputStream input = MethodHandles.lookup().lookupClass().
                    getClassLoader().getResourceAsStream(PROPS_FILE);
        if (input == null) {
            throw new RuntimeException("Can't find Toolkit properties file.");
        }
        try {
            // load a properties file
            props.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Main method for testing.
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
        initProperties();
        Enumeration<?> e = props.propertyNames();

        logger.info("All toolkit properties:");
        while (e.hasMoreElements()) {
          String key = (String) e.nextElement();
          logger.info(key + " -- " + props.getProperty(key));
        }
        logger.info("End of toolkit properties.");
    }

}
