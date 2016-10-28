/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Resource map entry model class.
 */
@Entity
@Table(name = ResourceMapEntry.TABLE_NAME)
/* Rather than including the text of the queries directly in the
 * annotations, we use constants defined in the class itself.
 * This way, they can be found (fully expanded!) in the generated Javadoc
 * in the "Constant Field Values" page. */
@NamedQueries({
    @NamedQuery(
            name = ResourceMapEntry.GET_RESOURCEMAPENTRIES_FOR_IRI,
            query = ResourceMapEntry.
                GET_RESOURCEMAPENTRIES_FOR_IRI_QUERY),
    @NamedQuery(
            name = ResourceMapEntry.
                GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI,
            query = ResourceMapEntry.
                GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI_QUERY),
    @NamedQuery(
            name = ResourceMapEntry.DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT,
            query = ResourceMapEntry.
                DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT_QUERY)
})
public class ResourceMapEntry {

    /** The name of the underlying database table.
     * Use this in the class's {@code @Table} annotation. */
    public static final String TABLE_NAME = "resource_map";

    /** Name of getResourceMapEntriesForIRI query. */
    public static final String GET_RESOURCEMAPENTRIES_FOR_IRI =
            "getResourceMapEntriesForIRI";
    /** Name of getResourceMapEntriesForIRI query's IRI parameter. */
    public static final String GET_RESOURCEMAPENTRIES_FOR_IRI_IRI =
            "iri";
    /** Query of getResourceMapEntriesForIRI query. */
    protected static final String GET_RESOURCEMAPENTRIES_FOR_IRI_QUERY =
            "SELECT rme FROM ResourceMapEntry rme WHERE rme.iri = :"
            + GET_RESOURCEMAPENTRIES_FOR_IRI_IRI;

    /** Name of getCurrentOwnedResourceMapEntriesForIRI query. */
    public static final String GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI =
            "getCurrentOwnedResourceMapEntriesForIRI";
    /** Name of getCurrentOwnedResourceMapEntriesForIRI query's
     *  IRI parameter. */
    public static final String
        GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI_IRI =
            "iri";
    /** Query of getCurrentOwnedResourceMapEntriesForIRI query. */
    protected static final String
        GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI_QUERY =
            "SELECT rme FROM ResourceMapEntry rme, "
            + "AccessPoint ap, "
            + "Version v "
            + "WHERE "
            + "rme.iri = :"
            + GET_CURRENT_OWNED_RESOURCEMAPENTRIES_FOR_IRI_IRI
            + " AND rme.owned = TRUE"
            + " AND rme.accessPointId = ap.id"
            + " AND ap.versionId = v.id"
            + " AND v.status = 'current'";

    /** Name of deleteResourceMapEntriesForAccessPoint query. */
    public static final String DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT =
            "deleteResourceMapEntriesForAccessPoint";
    /** Name of deleteResourceMapEntriesForAccessPoint query's accessPointId
     *  parameter. */
    public static final String
        DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT_ACCESSPOINTID =
            "accessPointId";
    /** Query of deleteResourceMapEntriesForAccessPoint query. */
    protected static final String
        DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT_QUERY =
            "DELETE FROM ResourceMapEntry rme "
            + "WHERE rme.accessPointId = :"
            + DELETE_RESOURCEMAPENTRIES_FOR_ACCESSPOINT_ACCESSPOINTID;

    /** id. */
    private Integer id;
    /** IRI. */
    private String iri;
    /** accessPointId. */
    private Integer accessPointId;

    /** Get the id.
     * @return The id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    /** Set the id.
     * @param anId the id
     */
    public void setId(final Integer anId) {
        id = anId;
    }

    /** Get the IRI.
     * @return The IRI
     */
    @Column(name = "iri", length = 65535)
    public String getIri() {
        return iri;
    }

    /** Set the IRI.
     * @param anIri the IRI
     */
    public void setIri(final String anIri) {
        iri = anIri;
    }

    /** Get the accessPointId.
     * @return The accessPointId
     */
    @Column(name = "access_point_id")
    public Integer getAccessPointId() {
        return accessPointId;
    }

    /** Set the accessPointId.
     * @param anAccessPointId the accessPointId
     */
    public void setAccessPointId(final Integer anAccessPointId) {
        accessPointId = anAccessPointId;
    }

    /** owned. */
    private Boolean owned;

    /** Get the value of owned.
     * @return The value of owned.
     */
    @Column(name = "owned")
    public Boolean getOwned() {
        return this.owned;
    }

    /** Set the value of owned.
     * @param anOwned The value of owned to set.
     */
    public void setOwned(final Boolean anOwned) {
        owned = anOwned;
    }

    /** resourceType. */
    private String resourceType;

    /** Get the value of resourceType.
     * @return The value of resourceType.
     */
    @Column(name = "resource_type", length = 65535)
    public String getResourceType() {
        return this.resourceType;
    }

    /** Set the value of resourceType.
     * @param aResourceType The value of resourceType to set.
     */
    public void setResourceType(final String aResourceType) {
        resourceType = aResourceType;
    }

    /** deprecated. */
    private Boolean deprecated;

    /** Get the value of deprecated.
     * @return The value of deprecated.
     */
    @Column(name = "deprecated")
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /** Set the value of deprecated.
     * @param aDeprecated The value of deprecated to set.
     */
    public void setDeprecated(final Boolean aDeprecated) {
        deprecated = aDeprecated;
    }

}
