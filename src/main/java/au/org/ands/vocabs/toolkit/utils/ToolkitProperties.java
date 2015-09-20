/** See the file "LICENSE" for the full license governing this code. */
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

    /** Base name of the main properties file. */
    private static final String PROPS_FILE = "toolkit.properties";

    /** Base name of the version properties file. */
    private static final String VERSION_PROPS_FILE = "version.properties";

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

    /** Get a toolkit property.
     * @param propName the property name
     * @param defaultValue a default value to use, if there is no
     * property with name propName
     * @return the properties
     */
    public static String getProperty(final String propName,
            final String defaultValue) {
        if (props == null) {
            initProperties();
        }
        return props.getProperty(propName, defaultValue);
    }

    /** Initialize the toolkit properties.
     */
    private static void initProperties() {
        props = new Properties();
        // Let user override the PROPS_FILE setting with the command line.
        // Useful for running standalone programs (i.e., using classes
        // that have a main() method).
        String propsFile = System.getProperty("PROPS_FILE");
        if (propsFile == null) {
            propsFile = PROPS_FILE;
        }
        InputStream input = MethodHandles.lookup().lookupClass().
                getClassLoader().getResourceAsStream(propsFile);
        if (input == null) {
            throw new RuntimeException("Can't find Toolkit properties file.");
        }
        InputStream input2 = MethodHandles.lookup().lookupClass().
                getClassLoader().getResourceAsStream(VERSION_PROPS_FILE);
        if (input2 == null) {
            throw new RuntimeException("Can't find Toolkit version "
                    + "properties file.");
        }
        try {
            // load a properties file
            props.load(input);
            props.load(input2);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("initProperties can't close properties file",
                            e);
                }
            }
            if (input2 != null) {
                try {
                    input2.close();
                } catch (IOException e) {
                    logger.error("initProperties can't close version "
                            + "properties file", e);
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
