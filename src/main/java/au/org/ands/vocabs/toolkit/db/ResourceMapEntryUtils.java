/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import au.org.ands.vocabs.toolkit.db.model.AccessPoint;
import au.org.ands.vocabs.toolkit.db.model.ResourceMapEntry;

/** Work with database resource map entries. */
public final class ResourceMapEntryUtils {

    /** Private constructor for a utility class. */
    private ResourceMapEntryUtils() {
    }

    /** The resource endpoint of a SISSVoc access point. Insert this
     * between the access point's portal_data's URI value and the resource
     * IRI, to get the final URL to send back as the redirect. */
    private static final String RESOURCE_ENDPOINT = "/resource?uri=";

    /** Get all resource map entries by IRI.
     * @param iri The IRI to be looked up.
     * @return A list of all resource map entries containing the IRI.
     */
    public static List<ResourceMapEntry> getResourceMapEntriesForIRI(
            final String iri) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery<ResourceMapEntry> q = em.createNamedQuery(
                ResourceMapEntry.GET_RESOURCEMAPENTRIES_FOR_IRI,
                ResourceMapEntry.class).
                setParameter(ResourceMapEntry.
                        GET_RESOURCEMAPENTRIES_FOR_IRI_IRI, iri);
        List<ResourceMapEntry> rme = q.getResultList();
        em.close();
        return rme;
    }

    /** Get all owned resource map entries by IRI, which come from "current"
     * versions.
     * @param iri The IRI to be looked up.
     * @return A list of all resource map entries containing the IRI,
     * for "current" versions only.
     */
    public static List<ResourceMapEntry>
        getCurrentOwnedResourceMapEntriesForIRI(
            final String iri) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery<ResourceMapEntry> q = em.createNamedQuery(
                ResourceMapEntry.GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI,
                ResourceMapEntry.class).
                setParameter(ResourceMapEntry.
                        GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI_IRI, iri);
        List<ResourceMapEntry> rme = q.getResultList();
        em.close();
        return rme;
    }

    /** Save a new resource map entry to the database.
     * @param rme The resource map entry to be saved.
     */
    public static void saveResourceMapEntry(final ResourceMapEntry rme) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.persist(rme);
        em.getTransaction().commit();
        em.close();
    }

    /** Create a resource map entry for an IRI and an access point.
     * @param iri The IRI to be mapped.
     * @param accessPointId The ID of the access point which defines the
     *      resource.
     * @param owned Whether the resource is owned by the owner of the vocabulary
     * @param resourceType The URL of the resource type
     * @param deprecated Whether the resource is deprecated
     */
    public static void addResourceMapEntry(final String iri,
            final Integer accessPointId,
            final Boolean owned,
            final String resourceType,
            final Boolean deprecated) {
        ResourceMapEntry rme = new ResourceMapEntry();
        rme.setIri(iri);
        rme.setAccessPointId(accessPointId);
        rme.setOwned(owned);
        rme.setResourceType(resourceType);
        rme.setDeprecated(deprecated);
        saveResourceMapEntry(rme);
    }

    /** Delete all resource map entries for an access point.
     * @param accessPointId The access point ID.
     */
    public static void deleteResourceMapEntriesForAccessPoint(
            final int accessPointId) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        Query q = em.createNamedQuery(
                ResourceMapEntry.DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT).
                setParameter(ResourceMapEntry.
                        DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT_ACCESSPOINTID,
                        accessPointId);
        q.executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    /** Get the IRI to which the ResourceMapEntry is to be redirected.
     * This is a URL which points to the SISSVoc resource endpoint
     * for this resource.
     * @param rme The ResourceMapEntry to be redirected
     * @return A String containing the URL to be sent back as the redirect.
     */
    public static String getRedirectForResourceMapEntry(
            final ResourceMapEntry rme) {
        AccessPoint ap =
                AccessPointUtils.getAccessPointById(rme.getAccessPointId());
        String portalData = AccessPointUtils.getPortalUri(ap);
        return portalData + RESOURCE_ENDPOINT + rme.getIri();
    }

}
