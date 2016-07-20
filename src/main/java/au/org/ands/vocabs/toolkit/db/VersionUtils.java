/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import au.org.ands.vocabs.toolkit.db.model.Version;

/** Work with database versions. */
public final class VersionUtils {

    /** Private constructor for a utility class. */
    private VersionUtils() {
    }

    /** Get version by version id.
     * @param id version id
     * @return the version
     */
    public static Version getVersionById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Version v = em.find(Version.class, id);
        em.close();
        return v;
    }

    /** Get all versions.
     * @return an array of all versions
     */
    public static List<Version> getAllVersions() {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery<Version> q = em.createNamedQuery(Version.GET_ALL_VERSIONS,
                Version.class);
        List<Version> v = q.getResultList();
        em.close();
        return v;
    }

}
