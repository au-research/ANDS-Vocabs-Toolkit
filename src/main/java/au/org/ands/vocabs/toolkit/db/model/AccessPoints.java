/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * AccessPoints model class.
 */
@Entity
@Table(name = "access_points")
@NamedQuery(
        name = AccessPoints.GET_ALL_ACCESSPOINTS,
        query = "SELECT ap FROM AccessPoints ap")
public class AccessPoints {

    /** Name of getAllAccessPoints query. */
    public static final String GET_ALL_ACCESSPOINTS = "getAllAccessPoints";

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
