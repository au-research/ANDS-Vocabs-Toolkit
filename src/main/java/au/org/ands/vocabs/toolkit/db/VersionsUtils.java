/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import javax.persistence.EntityManager;

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



}
