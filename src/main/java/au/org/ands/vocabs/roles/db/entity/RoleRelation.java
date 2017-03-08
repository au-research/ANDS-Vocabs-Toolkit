/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.roles.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * RoleRelation model class.
 */
@Entity
@EntityListeners(ReadOnly.class)
@Table(name = RoleRelation.TABLE_NAME)
/* Rather than including the text of the queries directly in the
 * annotations, we use constants defined in the class itself.
 * This way, they can be found (fully expanded!) in the generated Javadoc
 * in the "Constant Field Values" page. */
@NamedQueries({
    @NamedQuery(
            name = RoleRelation.GET_PARENT_ROLES_FOR_ROLEID,
            query = RoleRelation.GET_PARENT_ROLES_FOR_ROLEID_QUERY)
})
public class RoleRelation {

    /** The name of the underlying database table.
     * Use this in the class's {@code @Table} annotation. */
    public static final String TABLE_NAME = "role_relations";

    /** Name of getParentRolesForRoleId query. */
    public static final String GET_PARENT_ROLES_FOR_ROLEID =
            "getParentRolesForId";
    /** Name of getParentRolesForRoleId query's roleId parameter. */
    public static final String GET_PARENT_ROLES_FOR_ROLEID_ROLEID =
            "roleId";
    /** Query of getParentRolesForRoleId query. */
    protected static final String GET_PARENT_ROLES_FOR_ROLEID_QUERY =
            "SELECT DISTINCT"
            + " NEW au.org.ands.vocabs.roles.Role"
            + "(r.roleId, r.roleTypeId, r.name)"
            + " FROM RoleRelation rr, Role r"
            + " WHERE r.roleId = rr.parentRoleId"
            + " AND rr.childRoleId = :" + GET_PARENT_ROLES_FOR_ROLEID_ROLEID
            + " AND enabled = '1'"
            + " AND rr.parentRoleId <> :"
            + GET_PARENT_ROLES_FOR_ROLEID_ROLEID;

    /** id. */
    private Integer id;
    /** parentRoleId. */
    private String parentRoleId;
    /** childRoleId. */
    private String childRoleId;
    /** createdWho. */
    private String createdWho;
    /** createdWhen. */
    private Date createdWhen;
    /** modifiedWhen. */
    private Date modifiedWhen;
    /** modifiedWho. */
    private String modifiedWho;

    /** Get the id.
     * @return The id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    /** Set the id.
     * @param anId The id.
     */
    public void setId(final Integer anId) {
        id = anId;
    }

    /** Get the value of parentRoleId.
     * @return The value of parentRoleId.
     */
    @Column(name = "parent_role_id", length = 255)
    public String getParentRoleId() {
        return this.parentRoleId;
    }

    /** Set the value of parentRoleId.
     * @param aParentRoleId The value of parentRoleId to set.
     */
    public void setParentRoleId(final String aParentRoleId) {
        parentRoleId = aParentRoleId;
    }

    /** Get the value of childRoleId.
     * @return The value of childRoleId.
     */
    @Column(name = "child_role_id", length = 255)
    public String getChildRoleId() {
        return this.childRoleId;
    }

    /** Set the value of childRoleId.
     * @param aChildRoleId The value of childRoleId to set.
     */
    public void setChildRoleId(final String aChildRoleId) {
        childRoleId = aChildRoleId;
    }

    /** Get the value of createdWho.
     * @return The value of createdWho.
     */
    @Column(name = "created_who", length = 255)
    public String getCreatedWho() {
        return this.createdWho;
    }

    /** Set the value of createdWho.
     * @param aCreatedWho The value of createdWho to set.
     */
    public void setCreatedWho(final String aCreatedWho) {
        createdWho = aCreatedWho;
    }

    /** Get the value of createdWhen.
     * @return The value of createdWhen.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_when")
    public Date getCreatedWhen() {
        return this.createdWhen;
    }

    /** Set the value of createdWhen.
     * @param aCreatedWhen The value of createdWhen to set.
     */
    public void setCreatedWhen(final Date aCreatedWhen) {
        createdWhen = aCreatedWhen;
    }

    /** Get the value of modifiedWhen.
     * @return The value of modifiedWhen.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_when")
    public Date getModifiedWhen() {
        return this.modifiedWhen;
    }

    /** Set the value of modifiedWhen.
     * @param aModifiedWhen The value of modifiedWhen to set.
     */
    public void setModifiedWhen(final Date aModifiedWhen) {
        modifiedWhen = aModifiedWhen;
    }

    /** Get the value of modifiedWho.
     * @return The value of modifiedWho.
     */
    @Column(name = "modified_who", length = 255)
    public String getModifiedWho() {
        return this.modifiedWho;
    }

    /** Set the value of modifiedWho.
     * @param aModifiedWho The value of modifiedWho to set.
     */
    public void setModifiedWho(final String aModifiedWho) {
        modifiedWho = aModifiedWho;
    }

}
