/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class providing access to toolkit properties. */
public final class ToolkitProperties {

    /** Base name of the main properties file, if provided inside the
     * deployed webapp. If so provided, this file must be in the
     * directory WEB-INF/classes of the deployed webapp.
     * NB: If overriding with either the {@code PROPS_FILE} system property
     * or the {@code toolkit.properties} JNDI setting,
     * a relative path is relative to the <i>root</i> of the webapp.
     * So to get the same file as specified here, you would specify
     * a relative path {@code WEB-INF/classes/toolkit.properties}. */
    private static final String PROPS_FILE = "toolkit.properties";

    /** Base name of the version properties file.
     * This file must be in the directory {@code WEB-INF/classes} of the
     * deployed webapp. */
    private static final String VERSION_PROPS_FILE = "version.properties";

    /** Name of a system property, which, if specified, will cause
     * the classpath to be dumped at the beginning of
     * {@link #initProperties()}.
     */
    private static final String DUMP_CLASSPATH = "dumpClasspath";

    /** Name of a system property, which, if specified, will cause
     * the loaded properties to be dumped at the end of
     * {@link #initProperties()}.
     */
    private static final String DUMP_PROPERTY = "dumpProperties";

    /** Properties object. After initialization, contains all
     * properties loaded from
     * the toolkit properties file and the version properties file. */
    private static Properties props;

    /** Logger for this class. */
    private static Logger logger;

    /** This is a utility class. No instantiation. */
    private ToolkitProperties() {
    }

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

    /** Get the toolkit properties. (Forces initialization of the properties,
     * if that has not already happened.)
     * @return The properties.
     */
    public static Properties getProperties() {
        if (props == null) {
            initProperties();
        }
        return props;
    }

    /** Get the value of a toolkit property.
     * (Forces initialization of the properties,
     * if that has not already happened.)
     * @param propName The name of the property to fetch.
     * @return The value of the property.
     */
    public static String getProperty(final String propName) {
        if (props == null) {
            initProperties();
        }
        return props.getProperty(propName);
    }

    /** Get the value of a toolkit property. This version of the method
     * allows specifying a default value for the property, if one
     * has not been specified. (Forces initialization of the properties,
     * if that has not already happened.)
     * @param propName The name of the property to fetch.
     * @param defaultValue A default value to use, if there is no
     * property with name propName.
     * @return The value of the property.
     */
    public static String getProperty(final String propName,
            final String defaultValue) {
        if (props == null) {
            initProperties();
        }
        return props.getProperty(propName, defaultValue);
    }

    /** Initialize the toolkit properties. First, load the user-specified
     * properties file, "toolkit.properties", then the version properties
     * file, "version.properties". To find "toolkit.properties", priority
     * is given to a system property {@code PROPS_FILE}, e.g.,
     * {@code -DPROPS_FILE=/path/to/toolkit.properties}.
     * If there is no such property, an attempt is made to get
     * a Environment setting from JNDI, with key {@code toolkit.properties}.
     * If JNDI is not available (e.g., this is being called from
     * a standalone application), or there is no such setting in JNDI,
     * a final attempt is made to load the file specified by the value
     * of the field {@link #PROPS_FILE}
     * using the class loader. (When running in a servlet, the class loader
     * loads files relative to {@code WEB-INF/classes}; when running standalone
     * code, the class loader loads files relative to the current working
     * directory when the JVM was started.)
     */
    private static void initProperties() {
        logger.debug("In ToolkitProperties.initProperties()");
        if (System.getProperty(DUMP_CLASSPATH) != null) {
            // A dump of the classpath has been requested.
            logger.info("java.class.path = "
                    + System.getProperty("java.class.path"));
        }

        // Initialize props here, before loading any values into it.
        props = new Properties();
        // Get the ServletContext, if any. If running standalone code,
        // this will be null.
        ServletContext servletContext =
                ApplicationContextListener.getServletContext();
        if (servletContext == null) {
            // In production, this is definitely an error. But don't
            // flag as an error, as out-of-container unit testing
            // is supported. (E.g., see the main() method.)
            logger.info("servletContext is null!");
        }
        // InputStream for reading toolkit.properties.
        InputStream input = null;
        // Let user override the PROPS_FILE setting with the command line.
        // Useful for running standalone programs (i.e., using classes
        // that have a main() method).
        String propsFile = System.getProperty("PROPS_FILE");
        if (propsFile != null) {
            // Use "normal" file opening process.
            try {
                if (servletContext != null) {
                    logger.debug("Getting properties from a file, "
                            + "with servlet context");
                    String contextPath =
                            servletContext.getRealPath(File.separator);
                    Path path = Paths.get(contextPath).resolve(propsFile);
                    input = new FileInputStream(path.toFile());
                } else {
                    // No servlet context, so no resolution against
                    // a path. In this case, doesn't _have_ to be an
                    // absolute path, but easier if it is.
                    logger.debug("Getting properties from a file, "
                            + "without servlet context");
                    input = new FileInputStream(propsFile);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Error attempting to open Toolkit "
                        + "properties file with the path specified as a "
                        + "system property.");
            }
        } else {
            // See if there is an entry in JNDI.
            Context initialContext = null;
            Context envContext;
            try {
                initialContext = new InitialContext();
                envContext =
                        (Context) initialContext.lookup("java:comp/env");
                // See if there is a value.
                propsFile = (String)
                        envContext.lookup("toolkit.properties");
            } catch (NamingException e) {
                // No JNDI. So maybe not even running within Tomcat.
                // No problem.
            }
            if (propsFile != null) {
                // Use "normal" file opening process.
                try {
                    if (servletContext != null) {
                        String contextPath =
                                servletContext.getRealPath(File.separator);
                        Path path = Paths.get(contextPath).resolve(propsFile);
                        input = new FileInputStream(path.toFile());
                    } else {
                        // No servlet context, so no resolution against
                        // a path. In this case, doesn't _have_ to be an
                        // absolute path, but easier if it is.
                        input = new FileInputStream(propsFile);
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Error attempting to open "
                            + "Toolkit properties file with path "
                            + "specified as JNDI property.");
                }
            }
        }
        if (input == null) {
            // Haven't found the properties file either by system
            // property or JNDI property. So default to looking for
            // it within the webapp.
            propsFile = PROPS_FILE;
            logger.debug("Getting properties from the default file within "
                    + "the webapp");
            input = MethodHandles.lookup().lookupClass().
                getClassLoader().getResourceAsStream(propsFile);
        }
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
        if (System.getProperty(DUMP_PROPERTY) != null) {
            // A dump of all the properties has been requested.
            dumpProperties();
        }
    }

    /** Dump all the properties using INFO-level logging. If a
     * property name contains the word "password", its value
     * is not displayed. */
    private static void dumpProperties() {
        Enumeration<?> e = props.propertyNames();

        logger.info("All toolkit properties:");
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.matches(".*password.*")) {
                logger.info(key + ": value not displayed, for security.");
            } else {
                logger.info(key + " -- " + props.getProperty(key));
            }
        }
        logger.info("End of toolkit properties.");
    }


    /** Main method for testing.
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
        initProperties();
        dumpProperties();
    }

}
