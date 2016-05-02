/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;

import au.org.ands.vocabs.toolkit.db.DBContext;

/** Context listener for the Toolkit web application.
 */
public class ApplicationContextListener implements ServletContextListener {

    /** Period in ms to sleep at end of context destruction, to allow
     * time for watchdog threads to go away. */
    private static final int CLEANUP_SLEEP = 50;

    /** Keep a record of the ServletContext. This field is set only
     * by {@link #contextInitialized(ServletContextEvent)}. Therefore,
     * if running code as a standalone application, this will stay null.
     */
    private static ServletContext servletContext;

    /** Getter for the ServletContext. This will be null, if running
     * code as a standalone application.
     * @return The ServletContext of the web application.
     */
    public static ServletContext getServletContext() {
        return servletContext;
    }

    /** Listener for context initialization.
     *  Only does a basic logging of startup for now.
     * @param sce The ServletContextEvent.
     */
    @Override
    public final void contextInitialized(final ServletContextEvent sce) {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("In Toolkit contextInitialized()");
        servletContext = sce.getServletContext();
        if (servletContext == null) {
            logger.error("servletContext is null! This probably means "
                    + "a Tomcat JAR is missing.");
        }
    }

    /** Listener for context destruction.
     *  In particular, clean up database stuff.
     * @param sce The ServletContextEvent.
     */
    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {
        /** Logger for this class. */
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());

        logger.info("In Toolkit contextDestroyed()");
        // Code inspired by:
        //   http://stackoverflow.com/questions/3320400/to-prevent-
        //     a-memory-leak-the-jdbc-driver-has-been-forcibly-unregistered
        // First close any background tasks which may be using the DB ...

        // ... to be done ...

        // Carefully close the JPA EntityManagerFactory.
        dbShutdown();

        // Close any C3P0 DB connection pools.
        for (Object ds : C3P0Registry.getPooledDataSources()) {
            if (ds instanceof PooledDataSource) {
                try {
                    logger.info("Closing a PooledDataSource");
                    ((PooledDataSource) ds).close();
                } catch (SQLException e) {
                    logger.error("Failed to close a PooledDataSource", e);
                }
            } else {
                logger.info("Was looking for PooledDataSources, but found "
                        + "something else.");
            }
        }

        // Now deregister JDBC drivers in this context's ClassLoader:
        // Get the webapp's ClassLoader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Loop through all drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                // This driver was registered by the webapp's ClassLoader,
                // so deregister it:
                try {
                    logger.info("Deregistering JDBC driver {}", driver);
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    logger.error("Error deregistering JDBC driver {}",
                            driver, ex);
                }
            } else {
                // driver was not registered by the webapp's ClassLoader
                // and may be in use elsewhere
                logger.trace("Not deregistering JDBC driver {} as it does "
                        + "not belong to this webapp's ClassLoader", driver);
            }
        }

        // Invoke any remaining shutdown methods.
        ToolkitNetUtils.doShutdown();

        // When running tests, log4j may have started a thread;
        // shut it down.
        log4jShutdown();

        // Wait just a little bit for watchdog threads to close.
        // Didn't seem to need this until starting to make use of
        // the H2 database for testing.
        // The H2 watchdog thread has a loop containing
        // a 25 ms sleep. So by sleeping for 50 ms, we can allow it
        // time to go away by itself.
        try {
            Thread.sleep(CLEANUP_SLEEP);
        } catch (InterruptedException e) {
            // No problem.
        }
    }

    /** Shut down the database context. This method takes care not
     * to force the <i>creation</i> of the database context, if it
     * does not already exist. This is useful in the case of
     * stopping the web application before any use has been
     * made of the database. Without such care, context shutdown
     * may fail.
     */
    private void dbShutdown() {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        try {
            // See http://stackoverflow.com/questions/482633/
            //            in-java-is-it-possible-to-know-whether-a-
            //            class-has-already-been-loaded
            // But note that the approved answer uses
            // ClassLoader.getSystemClassLoader(), but this
            // is wrong in a servlet context. Use
            // getClass().getClassLoader() instead.
            // Use reflection to get the protected method findLoadedClass()
            // that otherwise can't be invoked directly.
            Method fLC = ClassLoader.class.getDeclaredMethod(
                    "findLoadedClass", new Class[] {String.class});
            fLC.setAccessible(true);
            ClassLoader classLoader = getClass().getClassLoader();
            // Now see if the DBContext class has been loaded
            Object dbContextClass =
                    fLC.invoke(classLoader,
                            "au.org.ands.vocabs.toolkit.db.DBContext");
            if (dbContextClass != null) {
                // The class has indeed been loaded already.
                logger.info("Calling DBContext.doShutdown");
                DBContext.doShutdown();
            } else {
                logger.info("DBContext not loaded; no shutdown required");
            }
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            logger.error("Exception while attempting to find "
                    + "DBContext class during context shutdown", e);
        }
    }

    /** Shut down log4j logging. Log4j is not normally used during
     * production, but is used by test harnesses. Without this,
     * context shutdown gives an error about a thread
     * "AsyncAppender-Dispatcher-Thread-1" not having been stopped.
     */
    private void log4jShutdown() {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        try {
            // See http://stackoverflow.com/questions/482633/
            //            in-java-is-it-possible-to-know-whether-a-
            //            class-has-already-been-loaded
            // But note that the approved answer uses
            // ClassLoader.getSystemClassLoader(), but this
            // is wrong in a servlet context. Use
            // getClass().getClassLoader() instead.
            // Use reflection to get the protected method findLoadedClass()
            // that otherwise can't be invoked directly.
            Method fLC = ClassLoader.class.getDeclaredMethod(
                    "findLoadedClass", new Class[] {String.class});
            fLC.setAccessible(true);
            ClassLoader classLoader = getClass().getClassLoader();
            // Now see if the LogManager class has been loaded
            Object log4jContextClass =
                    fLC.invoke(classLoader,
                            "org.apache.log4j.LogManager");
            if (log4jContextClass != null) {
                // The class has indeed been loaded already.
                logger.info("Calling log4j LogManager.shutdown()");
                // Use SuppressWarnings because we are not
                // importing the LogManager class, and so can't
                // provide the generic type parameter.
                @SuppressWarnings("rawtypes")
                Class log4jClass = Class.forName(
                        "org.apache.log4j.LogManager");
                // Ditto.
                @SuppressWarnings("unchecked")
                Method shutdown = log4jClass.getMethod("shutdown");
                shutdown.invoke(null);
            } else {
                logger.info("log4j not loaded; no shutdown required");
            }
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | ClassNotFoundException e) {
            logger.error("Exception while attempting to find "
                    + "log4j Manager class during context shutdown", e);
        }
    }


}
