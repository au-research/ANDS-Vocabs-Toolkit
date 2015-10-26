/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.lang.invoke.MethodHandles;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.DBContext;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;

/** Context listener for the Toolkit web application.
 */
public class ApplicationContextListener implements ServletContextListener {

    /** Listener for context initialization.
     *  Only does a basic logging of startup for now.
     * @param sce The ServletContextEvent.
     */
    @Override
    public final void contextInitialized(final ServletContextEvent sce) {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("In Toolkit contextInitialized()");
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

        // Close the JPA EntityManagerFactory.
        DBContext.doShutdown();

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
    }

}
