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
 * Access Point model class.
 */
@Entity
@Table(name = "access_points")
/* Rather than including the text of the queries directly in the
 * annotations, we use constants defined in the class itself.
 * This way, they can be found (fully expanded!) in the generated Javadoc
 * in the "Constant Field Values" page. */
@NamedQueries({
    @NamedQuery(
            name = AccessPoint.GET_ALL_ACCESSPOINTS,
            query = AccessPoint.GET_ALL_ACCESSPOINTS_QUERY),
    @NamedQuery(
            name = AccessPoint.GET_ACCESSPOINTS_FOR_VERSION,
            query = AccessPoint.GET_ACCESSPOINTS_FOR_VERSION_QUERY),
    @NamedQuery(
            name = AccessPoint.GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE,
            query = AccessPoint.GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_QUERY),
    @NamedQuery(
            name = AccessPoint.DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE,
            query = AccessPoint.DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_QUERY)
})
public class AccessPoint {

    /** Name of getAllAccessPoints query. */
    public static final String GET_ALL_ACCESSPOINTS = "getAllAccessPoints";
    /** Query of getAllAccessPoints query. */
    protected static final String GET_ALL_ACCESSPOINTS_QUERY =
            "SELECT ap FROM AccessPoint ap";

    /** Name of getAccessPointsForVersion query. */
    public static final String GET_ACCESSPOINTS_FOR_VERSION =
            "getAccessPointsForVersion";
    /** Name of getAccessPointsForVersion query's versionId parameter. */
    public static final String GET_ACCESSPOINTS_FOR_VERSION_VERSIONID =
            "versionId";
    /** Query of getAccessPointsForVersion query. */
    protected static final String GET_ACCESSPOINTS_FOR_VERSION_QUERY =
            "SELECT ap FROM AccessPoint ap WHERE ap.versionId = :"
            + GET_ACCESSPOINTS_FOR_VERSION_VERSIONID;

    /** Name of getAccessPointsForVersionAndType query. */
    public static final String GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE =
            "getAccessPointsForVersionAndType";
    /** Name of getAccessPointsForVersionAndType query's versionId
     *  parameter. */
    public static final String GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_VERSIONID =
            "versionId";
    /** Name of getAccessPointsForVersionAndType query's type parameter. */
    public static final String GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_TYPE =
            "type";
    /** Query of getAccessPointsForVersionAndType query. */
    protected static final String GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_QUERY =
            "SELECT ap FROM AccessPoint ap "
            + "WHERE ap.versionId = :"
            + GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_VERSIONID
            + " AND ap.type = :"
            + GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_TYPE;

    /** Name of deleteAccessPointsForVersionAndType query. */
    public static final String DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE =
            "deleteAccessPointsForVersionAndType";
    /** Name of deleteAccessPointsForVersionAndType query's versionId
     *  parameter. */
    public static final String
        DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_VERSIONID =
            "versionId";
    /** Name of deleteAccessPointsForVersionAndType query's type parameter. */
    public static final String DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_TYPE =
            "type";
    /** Query of deleteAccessPointsForVersionAndType query. */
    protected static final String
        DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_QUERY =
            "DELETE FROM AccessPoint ap "
            + "WHERE ap.versionId = :"
            + DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_VERSIONID
            + " AND ap.type = :"
            + DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_TYPE;

    /** id. */
    private Integer id;
    /** versionId. */
    private Integer versionId;
    /** type. */
    private String type;
    /** portal data. */
    private String portalData;
    /** toolkit data. */
    private String toolkitData;

    /** Value of the apiSparql access point type. */
    public static final String API_SPARQL_TYPE = "apiSparql";

    /** Value of the file access point type. */
    public static final String FILE_TYPE = "file";

    /** Value of the sesameDownload access point type. */
    public static final String SESAME_DOWNLOAD_TYPE = "sesameDownload";

    /** Value of the sissvoc access point type. */
    public static final String SISSVOC_TYPE = "sissvoc";

    /** Value of the webPage access point type. */
    public static final String WEBPAGE_TYPE = "webPage";

    /** Value of the system source value. */
    public static final String SYSTEM_SOURCE = "system";

    /** Value of the user source value. */
    public static final String USER_SOURCE = "user";

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

    /** Get the versionId.
     * @return The versionId
     */
    @Column(name = "version_id")
    public Integer getVersionId() {
        return versionId;
    }

    /** Set the versionId.
     * @param aVersionId the versionId
     */
    public void setVersionId(final Integer aVersionId) {
        versionId = aVersionId;
    }

    /** Get the type.
     * @return The type
     */
    @Column(name = "type", length = 45)
    public String getType() {
        return type;
    }

    /** Set the type.
     * @param aType the type
     */
    public void setType(final String aType) {
        type = aType;
    }

    /** Get the portal data.
     * @return The portal data
     */
    @Column(name = "portal_data", length = 65535)
    public String getPortalData() {
        return portalData;
    }

    /** Set the portal data.
     * @param aPortalData the data
     */
    public void setPortalData(final String aPortalData) {
        portalData = aPortalData;
    }

    /** Get the toolkit data.
     * @return The toolkit data
     */
    @Column(name = "toolkit_data", length = 65535)
    public String getToolkitData() {
        return toolkitData;
    }

    /** Set the toolkit data.
     * @param aToolkitData the data
     */
    public void setToolkitData(final String aToolkitData) {
        toolkitData = aToolkitData;
    }

}
