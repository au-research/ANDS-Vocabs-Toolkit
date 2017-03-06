/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.roles.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * AuthenticationBuiltIn model class.
 */
@Entity
@EntityListeners(ReadOnly.class)
@Table(name = AuthenticationBuiltIn.TABLE_NAME)
public class AuthenticationBuiltIn {

    /** The name of the underlying database table.
     * Use this in the class's {@code @Table} annotation. */
    public static final String TABLE_NAME = "authentication_built_in";

    /** id. */
    private Integer id;
    /** role id. */
    private String roleId;
    /** passphraseSHA1. */
    private String passphraseSHA1;
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

    /** Get the role id.
     * @return The role id.
     */
    @Column(name = "role_id", length = 255)
    public String getRoleId() {
        return this.roleId;
    }

    /** Set the role id.
     * @param aRoleId The role id.
     */
    public void setRoleId(final String aRoleId) {
        roleId = aRoleId;
    }

    /** Get the passphraseSHA1.
     * @return The passphraseSHA1
     */
    @Column(name = "passphrase_sha1", length = 255)
    public String getPassphraseSHA1() {
        return passphraseSHA1;
    }

    /** Set the passphraseSHA1.
     * @param aPassphraseSHA1 The passphraseSHA1.
     */
    public void setname(final String aPassphraseSHA1) {
        passphraseSHA1 = aPassphraseSHA1;
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
