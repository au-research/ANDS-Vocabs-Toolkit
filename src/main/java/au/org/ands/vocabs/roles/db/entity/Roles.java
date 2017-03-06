/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.roles.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Roles model class.
 */
@Entity
@EntityListeners(ReadOnly.class)
@Table(name = Roles.TABLE_NAME)
public class Roles {

    /** The name of the underlying database table.
     * Use this in the class's {@code @Table} annotation. */
    public static final String TABLE_NAME = "roles";

    /** id. */
    private Integer id;
    /** role id. */
    private String roleId;
    /** role type id. */
    private RoleTypeId roleTypeId;
    /** name. */
    private String name;
    /** authenticationServiceId. */
    private String authenticationServiceId;
    /** enabled. */
    private String enabled;
    /** createdWhen. */
    private Date createdWhen;
    /** createdWho. */
    private String createdWho;
    /** modifiedWho. */
    private String modifiedWho;
    /** modifiedWhen. */
    private Date modifiedWhen;
    /** lastLogin. */
    private Date lastLogin;
    /** sharedToken. */
    private String sharedToken;
    /** persistentId. */
    private String persistentId;
    /** email. */
    private String email;
    /** oauthAccessToken. */
    private String oauthAccessToken;
    /** oauthData. */
    private String oauthData;

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

    /** Get the role id.
     * @return The role id.
     */
    @Column(name = "role_id", length = 255)
    public String getRoleId() {
        return this.roleId;
    }

    /** Set the role id.
     * @param aRoleId the role id.
     */
    public void setRoleId(final String aRoleId) {
        roleId = aRoleId;
    }

    /** Get the role type id.
     * @return The role type id.
     */
    @Column(name = "role_type_id", length = 20)
    @Enumerated(EnumType.STRING)
    public RoleTypeId getRoleTypeId() {
        return this.roleTypeId;
    }

    /** Set the role type id.
     * @param aRoleTypeId the role type id.
     */
    public void setRoleTypeId(final RoleTypeId aRoleTypeId) {
        roleTypeId = aRoleTypeId;
    }

    /** Get the name.
     * @return The id
     */
    @Column(name = "name", length = 255)
    public String getName() {
        return name;
    }

    /** Set the name.
     * @param aName the name
     */
    public void setName(final String aName) {
        name = aName;
    }

    /** Get the value of authenticationServiceId.
     * @return The value of authenticationServiceId.
     */
    @Column(name = "authentication_service_id", length = 32)
    public String getAuthenticationServiceId() {
        return this.authenticationServiceId;
    }

    /** Set the value of authenticationServiceId.
     * @param aAuthenticationServiceId The value of authenticationServiceId
     *      to set.
     */
    public void setAuthenticationServiceId(
            final String aAuthenticationServiceId) {
        authenticationServiceId = aAuthenticationServiceId;
    }

    /** Get the value of enabled.
     * @return The value of enabled.
     */
    @Column(name = "enabled", length = 1)
    public String getEnabled() {
        return this.enabled;
    }

    /** Set the value of enabled.
     * @param aEnabled The value of enabled to set.
     */
    public void setEnabled(final String aEnabled) {
        enabled = aEnabled;
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

    /** Get the value of lastLogin.
     * @return The value of lastLogin.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    public Date getLastLogin() {
        return this.lastLogin;
    }

    /** Set the value of lastLogin.
     * @param aLastLogin The value of lastLogin to set.
     */
    public void setLastLogin(final Date aLastLogin) {
        lastLogin = aLastLogin;
    }

    /** Get the value of sharedToken.
     * @return The value of sharedToken.
     */
    @Column(name = "shared_token", length = 255)
    public String getSharedToken() {
        return this.sharedToken;
    }

    /** Set the value of sharedToken.
     * @param aSharedToken The value of sharedToken to set.
     */
    public void setSharedToken(final String aSharedToken) {
        sharedToken = aSharedToken;
    }

    /** Get the value of persistentId.
     * @return The value of persistentId.
     */
    @Column(name = "persistent_id", length = 255)
    public String getPersistentId() {
        return this.persistentId;
    }

    /** Set the value of persistentId.
     * @param aPersistentId The value of persistentId to set.
     */
    public void setPersistentId(final String aPersistentId) {
        persistentId = aPersistentId;
    }

    /** Get the value of email.
     * @return The value of email.
     */
    @Column(name = "email", length = 255)
    public String getEmail() {
        return this.email;
    }

    /** Set the value of email.
     * @param aEmail The value of email to set.
     */
    public void setEmail(final String aEmail) {
        email = aEmail;
    }

    /** Get the value of oauthAccessToken.
     * @return The value of oauthAccessToken.
     */
    @Column(name = "oauth_access_token", length = 255)
    public String getOauthAccessToken() {
        return this.oauthAccessToken;
    }

    /** Set the value of oauthAccessToken.
     * @param aOauthAccessToken The value of oauthAccessToken to set.
     */
    public void setOauthAccessToken(final String aOauthAccessToken) {
        oauthAccessToken = aOauthAccessToken;
    }

    /** Get the value of oauthData.
     * @return The value of oauthData.
     */
    @Column(name = "oauth_data", length = 65535)
    public String getOauthData() {
        return this.oauthData;
    }

    /** Set the value of oauthData.
     * @param aOauthData The value of oauthData to set.
     */
    public void setOauthData(final String aOauthData) {
        oauthData = aOauthData;
    }

}
