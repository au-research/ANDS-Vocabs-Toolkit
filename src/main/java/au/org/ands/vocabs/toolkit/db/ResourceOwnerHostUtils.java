/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import au.org.ands.vocabs.toolkit.db.model.ResourceOwnerHost;

/** Work with database resource owner host entries. */
public final class ResourceOwnerHostUtils {

    /** Private constructor for a utility class. */
    private ResourceOwnerHostUtils() {
    }

    /** Get all resource owner hosts by owner.
     * @param owner The owner to be looked up.
     * @return A list of all resource map entries belonging to the owner.
     */
    public static List<ResourceOwnerHost>
    getResourceOwnerHostMapEntriesForOwner(
            final String owner) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery<ResourceOwnerHost> q = em.createNamedQuery(
                ResourceOwnerHost.GET_RESOURCEOWNERHOSTS_FOR_OWNER,
                ResourceOwnerHost.class).
                setParameter(ResourceOwnerHost.
                        GET_RESOURCEOWNERHOSTS_FOR_OWNER_OWNER, owner);
        q = TemporalUtils.setCurrentDatetimeParameterNow(q);
        List<ResourceOwnerHost> rme = q.getResultList();
        em.close();
        return rme;
    }

    /** Save a new resource owner host entry to the database.
     * @param roh The resource owner host entry to be saved.
     */
    public static void saveResourceOwnerHost(final ResourceOwnerHost roh) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.persist(roh);
        em.getTransaction().commit();
        em.close();
    }

    /** Delete a resource owner host by ID.
     * @param rohId The resource owner host ID.
     */
    public static void deleteResourceOwnerHostById(
            final int rohId) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        Query q = em.createNamedQuery(
                ResourceOwnerHost.DELETE_RESOURCEOWNERHOSTBYID).
                setParameter(ResourceOwnerHost.
                        DELETE_RESOURCEOWNERHOSTBYID_ROHID,
                        rohId);
        q.executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

}
