/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import au.org.ands.vocabs.toolkit.db.model.Versions;

/** Work with database versions. */
public final class VersionsUtils {

    /** Private constructor for a utility class. */
    private VersionsUtils() {
    }

    /** Get version by version id.
     * @param id version id
     * @return the version
     */
    public static Versions getVersionById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Versions v = em.find(Versions.class, id);
        em.close();
        return v;
    }

    /** Get all versions.
     * @return an array of all versions
     */
    public static List<Versions> getAllVersions() {
        EntityManager em = DBContext.getEntityManager();
        Query q = em.createQuery("select v from Versions v");
        @SuppressWarnings("unchecked")
        List<Versions> v = q.getResultList();
        em.close();
        return v;
    }

}
