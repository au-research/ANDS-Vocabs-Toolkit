/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.roles.db.utils;

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

import au.org.ands.vocabs.toolkit.utils.ApplicationContextListener;

/** Utility class providing access to roles properties. */
public final class RolesProperties {

    /** Base name of the roles properties file, if provided inside the
     * deployed webapp. If so provided, this file must be in the
     * directory WEB-INF/classes of the deployed webapp.
     * NB: If overriding with either the {@code ROLES_PROPS_FILE}
     * system property
     * or the {@code roles.properties} JNDI setting,
     * a relative path is relative to the <i>root</i> of the webapp.
     * So to get the same file as specified here, you would specify
     * a relative path {@code WEB-INF/classes/roles.properties}. */
    private static final String ROLES_PROPS_FILE = "roles.properties";

    /** Name of a system property, which, if specified, will cause
     * the loaded properties to be dumped at the end of
     * {@link #initProperties()}.
     */
    private static final String ROLES_DUMP_PROPERTY = "rolesDumpProperties";

    /** Properties object. After initialization, contains all
     * properties loaded from
     * the roles properties file and the version properties file. */
    private static Properties props;

    /** Logger for this class. */
    private static Logger logger;

    /** This is a utility class. No instantiation. */
    private RolesProperties() {
    }

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

    /** Get the roles properties. (Forces initialization of the properties,
     * if that has not already happened.)
     * @return The properties.
     */
    public static Properties getProperties() {
        if (props == null) {
            initProperties();
        }
        return props;
    }

    /** Get the value of a roles property.
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

    /** Get the value of a roles property. This version of the method
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

    /** Initialize the roles properties. Load the user-specified
     * properties file, "roles.properties".
     * To find "roles.properties", priority
     * is given to a system property {@code ROLES_PROPS_FILE}, e.g.,
     * {@code -DROLES_PROPS_FILE=/path/to/roles.properties}.
     * If there is no such property, an attempt is made to get
     * a Environment setting from JNDI, with key {@code roles.properties}.
     * If JNDI is not available (e.g., this is being called from
     * a standalone application), or there is no such setting in JNDI,
     * a final attempt is made to load the file specified by the value
     * of the field {@link #ROLES_PROPS_FILE}
     * using the class loader. (When running in a servlet, the class loader
     * loads files relative to {@code WEB-INF/classes}; when running standalone
     * code, the class loader loads files relative to the current working
     * directory when the JVM was started.)
     */
    private static void initProperties() {
        logger.debug("In RolesProperties.initProperties()");
        // Initialize props here, before loading any values into it.
        props = new Properties();
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
        // InputStream for reading roles.properties.
        InputStream input = null;
        // Let user override the ROLES_PROPS_FILE setting with the command line.
        // Useful for running standalone programs (i.e., using classes
        // that have a main() method).
        String propsFile = System.getProperty("ROLES_PROPS_FILE");
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
                throw new RuntimeException("Error attempting to open roles "
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
                        envContext.lookup("roles.properties");
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
                            + "roles properties file with path "
                            + "specified as JNDI property.");
                }
            }
        }
        if (input == null) {
            // Haven't found the properties file either by system
            // property or JNDI property. So default to looking for
            // it within the webapp.
            propsFile = ROLES_PROPS_FILE;
            logger.debug("Getting properties from the default file within "
                    + "the webapp");
            input = MethodHandles.lookup().lookupClass().
                getClassLoader().getResourceAsStream(propsFile);
        }
        if (input == null) {
            throw new RuntimeException("Can't find roles properties file.");
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
                    logger.error("initProperties can't close properties file",
                            e);
                }
            }
        }
        if (System.getProperty(ROLES_DUMP_PROPERTY) != null) {
            // A dump of all the properties has been requested.
            dumpProperties();
        }
    }

    /** Dump all the properties using INFO-level logging. If a
     * property name contains the word "password", its value
     * is not displayed. */
    private static void dumpProperties() {
        Enumeration<?> e = props.propertyNames();

        logger.info("All roles properties:");
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.matches(".*password.*")) {
                logger.info(key + ": value not displayed, for security.");
            } else {
                logger.info(key + " -- " + props.getProperty(key));
            }
        }
        logger.info("End of roles properties.");
    }


    /** Main method for testing.
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
        initProperties();
        dumpProperties();
    }

}
