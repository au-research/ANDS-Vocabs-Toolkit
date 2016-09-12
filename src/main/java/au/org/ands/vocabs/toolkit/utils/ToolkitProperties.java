/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class providing access to toolkit properties.
 * Properties are <i>implemented</i> using Apache Commons Configuration,
 * but <i>accessed</i> as though they were Java Properties.
 * The reason for using Commons Configuration is to support its
 * extra features. For now, the one extra feature taken advantage
 * of is interpolation of variables. That is, a property value
 * may include the value of another property using the
 * <code>${...}</code> syntax.
 */
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
     * deployed webapp. For Arquillian testing, the location of the
     * file in "client" mode (outside the servlet container)
     * can be overridden by setting the
     * {@code VERSION_PROPS_FILE} system property. */
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

    /** All loaded properties. After initialization, contains all
     * properties loaded from
     * the toolkit properties file and the version properties file.
     * This is the "definitive" copy of the properties. The
     * field {@link ToolkitProperties#propsAsProperties} is derived
     * from this. */
    private static AbstractFileConfiguration props;

    /** A copy of all loaded properties, derived from
     * {@link ToolkitProperties#props}. This field is used to provide
     * a cached copy for quick access. */
    private static Properties propsAsProperties;


    /** Logger for this class. */
    private static Logger logger;

    /** This is a utility class. No instantiation. */
    private ToolkitProperties() {
    }

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

    /** Get all of the toolkit properties, as a Properties object.
     * Forces initialization of the properties,
     * if that has not already happened.
     * @return The toolkit properties, as a Properties object.
     */
    public static Properties getProperties() {
        if (props == null) {
            initProperties();
        }
        return propsAsProperties;
    }

    /** Get the value of a toolkit property.
     * Forces initialization of the properties,
     * if that has not already happened.
     * @param propName The name of the property to fetch.
     * @return The value of the property.
     */
    public static String getProperty(final String propName) {
        if (props == null) {
            initProperties();
        }
        return props.getString(propName);
    }

    /** Get the value of a toolkit property. This version of the method
     * allows specifying a default value for the property, if one
     * has not been specified. Forces initialization of the properties,
     * if that has not already happened.
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
        return props.getString(propName, defaultValue);
    }

    // On a rainy day, refactor this so that the suppression is
    // no longer required.
    //CHECKSTYLE:OFF: MethodLength
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
     * Specifically to support testing in Arquillian, in which this code
     * is executed <i>both</i> inside and outside a servlet container,
     * a system property {@code PROPS_FILE_CLIENT_MODE} is supported, e.g.
     * using
     * {@code -DPROPS_FILE_CLIENT_MODE=conf/toolkit-h2.properties},
     * whose value overrides that of {@code PROPS_FILE} when
     * this code is run outside a servlet container.
     */
    private static void initProperties() {
        logger.debug("In ToolkitProperties.initProperties()");
        if (System.getProperty(DUMP_CLASSPATH) != null) {
            // A dump of the classpath has been requested.
            logger.info("java.class.path = "
                    + System.getProperty("java.class.path"));
        }

        // Initialize props here, before loading any values into it.
        props = new PropertiesConfiguration();
        // Don't allow the presence of a comma to create multiple
        // instances of a property.
        props.setListDelimiter((char) 0);

        // Get the ServletContext, if any. If running standalone code,
        // this will be null.
        ServletContext servletContext = null;
        try {
            servletContext = ApplicationContextListener.getServletContext();
        } catch (NoClassDefFoundError e) {
            // This means we're probably running a standalone application,
            // not running inside a container. So the Servlet API JAR
            // is not even in the classpath.
        }
        if (servletContext == null) {
            // In production, running in a servlet container,
            // this is definitely an error.
            // But don't flag as an error, as out-of-container applications,
            // and unit testing are supported.
            // (E.g., see the main() method, and the RewriteCurrent class.)
            logger.info("servletContext is null. Standalone application?");
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

                    // Support Arquillian testing, in which we need to
                    // get the properties file from two quite different
                    // places.
                    String propsFileClientMode =
                            System.getProperty("PROPS_FILE_CLIENT_MODE");
                    if (propsFileClientMode != null) {
                        logger.debug("Overriding properties file "
                                + "for client mode");
                        propsFile = propsFileClientMode;
                    }

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

        // Now fetch the version properties file.
        String versionPropsFile = VERSION_PROPS_FILE;
        InputStream input2 = null;
        if (servletContext == null) {
            // For Arquillian testing, support VERSION_PROPS_FILE system
            // property to override for client mode (outside the servlet
            // container).
            String versionPropsFileClientMode =
                    System.getProperty("VERSION_PROPS_FILE");
            if (versionPropsFileClientMode != null) {
                logger.debug("Overriding version properties file for "
                        + "client mode.");
                versionPropsFile = versionPropsFileClientMode;
                try {
                    input2 = new FileInputStream(versionPropsFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(
                            "Error attempting to open version "
                            + "properties file with the path specified as a "
                            + "system property.");
                }
            }
        }

        if (input2 == null) {
            // The "normal" way to load the version properties file.
            input2 = MethodHandles.lookup().lookupClass().
                    getClassLoader().getResourceAsStream(versionPropsFile);
        }
        if (input2 == null) {
            throw new RuntimeException("Can't find Toolkit version "
                    + "properties file.");
        }
        try {
            // load a properties file
            props.load(new InputStreamReader(input));
            props.load(new InputStreamReader(input2));
            // Support interpolation of properties using ${...} syntax.
            props = (AbstractFileConfiguration)
                    props.interpolatedConfiguration();
            // And now convert into Properties format for cached access.
            propsAsProperties = ConfigurationConverter.getProperties(props);
        } catch (ConfigurationException ex) {
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
    //CHECKSTYLE:ON: MethodLength

    /** Dump all the properties using INFO-level logging. If a
     * property name contains the word "password", its value
     * is not displayed. */
    private static void dumpProperties() {
        Enumeration<?> e = getProperties().propertyNames();

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
