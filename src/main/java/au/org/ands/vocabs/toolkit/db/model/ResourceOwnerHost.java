/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import static au.org.ands.vocabs.toolkit.db.TemporalUtils.E1;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import au.org.ands.vocabs.toolkit.db.TemporalUtils;

/**
 * Resource owner host model class.
 */
@Entity
@Table(name = "resource_owner_hosts")
/* Rather than including the text of the queries directly in the
 * annotations, we use constants defined in the class itself.
 * This way, they can be found (fully expanded!) in the generated Javadoc
 * in the "Constant Field Values" page. */
@NamedQueries({
    @NamedQuery(
            name = ResourceOwnerHost.GET_RESOURCEOWNERHOSTS_FOR_OWNER,
            query = ResourceOwnerHost.
                GET_RESOURCEOWNERHOSTS_FOR_OWNER_QUERY),
    @NamedQuery(
            name = ResourceOwnerHost.DELETE_RESOURCEOWNERHOSTBYID,
            query = ResourceOwnerHost.
                DELETE_RESOURCEOWNERHOSTBYID_QUERY),
})
public class ResourceOwnerHost {

    /** Name of getResourceOwnerHostsForOwner query. */
    public static final String GET_RESOURCEOWNERHOSTS_FOR_OWNER =
            "getResourceOwnerHostsForOwner";
    /** Name of getResourceOwnerHostsForOwner query's owner parameter. */
    public static final String GET_RESOURCEOWNERHOSTS_FOR_OWNER_OWNER =
            "owner";
    /** Query of getResourceMapEntriesForIRI query. */
    protected static final String GET_RESOURCEOWNERHOSTS_FOR_OWNER_QUERY =
            "SELECT " + E1 + " FROM ResourceOwnerHost " + E1
            + " WHERE " + E1 + ".owner = :"
            + GET_RESOURCEOWNERHOSTS_FOR_OWNER_OWNER
            + TemporalUtils.TEMPORAL_QUERY_TEMPLATE_CURRENTLY_VALID_SUFFIX_E1;

    /** Name of deleteResourceOwnerHostById query. */
    public static final String DELETE_RESOURCEOWNERHOSTBYID =
            "deleteResourceOwnerHostById";
    /** Name of deleteResourceOwnerHostById query's rohId parameter. */
    public static final String DELETE_RESOURCEOWNERHOSTBYID_ROHID = "rohId";
    /** Query of deleteResourceOwnerHostById query. */
    protected static final String DELETE_RESOURCEOWNERHOSTBYID_QUERY =
            "DELETE FROM ResourceOwnerHost roh "
            + "WHERE roh.id = :" + DELETE_RESOURCEOWNERHOSTBYID_ROHID;

    /** id. */
    private Integer id;

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

    /** startDate. */
    private LocalDateTime startDate;

    /** Get the value of startDate.
     * @return The value of startDate.
     */
    @Column(name = "start_date", length = 255)
    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    /** Set the value of startDate.
     * @param aStartDate The value of startDate to set.
     */
    public void setStartDate(final LocalDateTime aStartDate) {
        startDate = aStartDate;
    }

    /** endDate. */
    private LocalDateTime endDate;

    /** Get the value of endDate.
     * @return The value of endDate.
     */
    @Column(name = "end_date", length = 255)
    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    /** Set the value of endDate.
     * @param aEndDate The value of endDate to set.
     */
    public void setEndDate(final LocalDateTime aEndDate) {
        endDate = aEndDate;
    }

    /** owner. */
    private String owner;

    /** Get the value of owner.
     * @return The value of owner.
     */
    @Column(name = "owner", length = 255)
    public String getOwner() {
        return this.owner;
    }

    /** Set the value of owner.
     * @param aOwner The value of owner to set.
     */
    public void setOwner(final String aOwner) {
        owner = aOwner;
    }

    /** host. */
    private String host;

    /** Get the value of host.
     * @return The value of host.
     */
    @Column(name = "host", length = 255)
    public String getHost() {
        return this.host;
    }

    /** Set the value of host.
     * @param aHost The value of host to set.
     */
    public void setHost(final String aHost) {
        host = aHost;
    }

}
