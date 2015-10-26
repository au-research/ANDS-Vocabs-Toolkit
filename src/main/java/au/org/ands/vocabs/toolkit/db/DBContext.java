/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Provide access to the database. */
public final class DBContext {

    /** Access to persistence context. */
    private static EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("ANDS-Vocabs-Toolkit",
                    ToolkitProperties.getProperties());

    /** Private constructor for a utility class. */
    private DBContext() {
    }

    /** Return an entity manager.
     * @return an entity manager
     */
    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    /** Prepare for shutdown. Call this only in webapp context shutdown!
     */
    public static void doShutdown() {
        entityManagerFactory.close();
        entityManagerFactory = null;
    }


}
