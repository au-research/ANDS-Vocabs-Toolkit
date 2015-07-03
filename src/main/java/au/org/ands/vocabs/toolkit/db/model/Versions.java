/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * VersionsUtils model class.
 */
@Entity
@Table(name = "versions",
uniqueConstraints = @UniqueConstraint(columnNames = "repository_id"))
public class Versions {

    /** id. */
    private Integer id;
    /** title. */
    private String title;
    /** status. */
    private String status;
    /** releaseDate. */
    private Date releaseDate;
    /** vocabId. */
    private Integer vocabId;
    /** data. */
    private String data;
    /** respositoryId. */
    private String repositoryId;

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
     * @return The title
     */
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    /** Set the title.
     * @param aTitle the title
     */
    public void setTitle(final String aTitle) {
        title = aTitle;
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

    /** Get the release date.
     * @return The release date
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "release_date")
    public Date getReleaseDate() {
        return releaseDate;
    }

    /** Set the release date.
     * @param aReleaseDate the release date
     */
    public void setReleaseDate(final Date aReleaseDate) {
        releaseDate = aReleaseDate;
    }

    /** Get the vocab id.
     * @return The vocab id
     */
    @Column(name = "vocab_id")
    public Integer getVocabId() {
        return vocabId;
    }

    /** Set the vocab id.
     * @param aVocabId the vocab id
     */
    public void setVocabId(final Integer aVocabId) {
        vocabId = aVocabId;
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

    /** Get the repository id.
     * @return The repository id
     */
    @Column(name = "repository_id", unique = true, length = 128)
    public String getRepositoryId() {
        return repositoryId;
    }

    /** Set the repository id.
     * @param aRepositoryId the repository id
     */
    public void setRepositoryId(final String aRepositoryId) {
        repositoryId = aRepositoryId;
    }

}
