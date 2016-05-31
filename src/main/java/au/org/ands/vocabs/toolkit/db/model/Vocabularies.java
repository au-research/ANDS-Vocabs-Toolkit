/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import au.org.ands.vocabs.toolkit.db.DBContext;

/**
 * Vocabularies model class.
 */
@PersistenceUnit(unitName = DBContext.UNIT_NAME)
@Entity
@Table(name = "vocabularies")
public class Vocabularies {

    /** id. */
    private Integer id;
    /** title. */
    private String title;
    /** slug. */
    private String slug;
    /** description. */
    private String description;
    /** createdDate. */
    private Date createdDate;
    /** modifiedDate. */
    private Date modifiedDate;
    /** modifiedWho. */
    private String modifiedWho;
    /** licence. */
    private String licence;
    /** poolPartyId. */
    private String poolPartyId;
    /** data. */
    private String data;
    /** owner. */
    private String owner;
    /** userOwner. */
    private String userOwner;
    /** status. */
    private String status;

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

    /** Get the title.
     * @return The id
     */
    @Column(name = "title", length = 255)
    public String getTitle() {
        return title;
    }

    /** Set the title.
     * @param aTitle the title
     */
    public void setTitle(final String aTitle) {
        title = aTitle;
    }

    /** Get the slug.
     * @return The slug
     */
    @Column(name = "slug", length = 255)
    public String getSlug() {
        return slug;
    }

    /** Set the slug.
     * @param aSlug the slug
     */
    public void setSlug(final String aSlug) {
        slug = aSlug;
    }

    /** Get the description.
     * @return The description
     */
    @Column(name = "description", length = 65535)
    public String getDescription() {
        return description;
    }

    /** Set the description.
     * @param aDescription the description
     */
    public void setDescription(final String aDescription) {
        description = aDescription;
    }

    /** Get the created date.
     * @return The created date
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    public Date getCreatedDate() {
        return createdDate;
    }

    /** Set the created data.
     * @param aCreatedDate the created date
     */
    public void setCreatedDate(final Date aCreatedDate) {
        createdDate = aCreatedDate;
    }

    /** Get the modified date.
     * @return The modified date
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /** Set the modified date.
     * @param aModifiedDate the modified date
     */
    public void setModifiedDate(final Date aModifiedDate) {
        modifiedDate = aModifiedDate;
    }

    /** Get the modified who.
     * @return The modified who
     */
    @Column(name = "modified_who", length = 45)
    public String getModifiedWho() {
        return modifiedWho;
    }

    /** Set the modified who.
     * @param aModifiedWho the modified who
     */
    public void setModifiedWho(final String aModifiedWho) {
        modifiedWho = aModifiedWho;
    }

    /** Get the licence.
     * @return The licence
     */
    @Column(name = "licence", length = 65535)
    public String getLicence() {
        return licence;
    }

    /** Set the licence.
     * @param aLicence the licence
     */
    public void setLicence(final String aLicence) {
        licence = aLicence;
    }

    /** Get the PoolParty id.
     * @return The PoolParty id
     */
    @Column(name = "pool_party_id", length = 45)
    public String getPoolPartyId() {
        return poolPartyId;
    }

    /** Set the PoolParty id.
     * @param aPoolPartyId the PoolParty id
     */
    public void setPoolPartyId(final String aPoolPartyId) {
        poolPartyId = aPoolPartyId;
    }

    /** Get the data.
     * @return The data
     */
    @Column(name = "data", length = 65535)
    public String getData() {
        return data;
    }

    /** Set the data.
     * @param aData the data
     */
    public void setData(final String aData) {
        data = aData;
    }

    /** Get the owner.
     * @return The owner
     */
    @Column(name = "owner", length = 255)
    public String getOwner() {
        return owner;
    }

    /** Set the owner.
     * @param anOwner the owner
     */
    public void setOwner(final String anOwner) {
        owner = anOwner;
    }

    /** Get the user owner.
     * @return The user owner
     */
    @Column(name = "user_owner", length = 255)
    public String getUserOwner() {
        return userOwner;
    }

    /** Set the user owner.
     * @param aUserOwner the user owner
     */
    public void setUserOwner(final String aUserOwner) {
        userOwner = aUserOwner;
    }

    /** Get the status.
     * @return The status
     */
    @Column(name = "status", length = 45)
    public String getStatus() {
        return status;
    }

    /** Set the status.
     * @param aStatus the status
     */
    public void setStatus(final String aStatus) {
        status = aStatus;
    }

}
