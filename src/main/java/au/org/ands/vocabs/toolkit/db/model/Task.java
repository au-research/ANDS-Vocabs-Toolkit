/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Task model class.
 */
@Entity
@Table(name = "task")
@NamedQuery(
        name = Task.GET_ALL_TASKS,
        query = "SELECT t FROM Task t")
@XmlRootElement
public class Task {

    /** Name of getAllTasks query. */
    public static final String GET_ALL_TASKS = "getAllTasks";

    /** id. */
    private Integer id;
    /** status. */
    private String status;
    /** params. */
    private String params;
    /** response. */
    private String response;
    /** vocabularyId. */
    private Integer vocabularyId;
    /** versionId. */
    private Integer versionId;

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

    /** Get the params.
     * @return The params
     */
    @Column(name = "params")
    public String getParams() {
        return params;
    }

    /** Set the params.
     * @param aParams the params
     */
    public void setParams(final String aParams) {
        params = aParams;
    }

    /** Get the response.
     * @return The response
     */
    @Column(name = "response", length = 45)
    public String getResponse() {
        return response;
    }

    /** Set the response.
     * @param aResponse the response
     */
    public void setResponse(final String aResponse) {
        response = aResponse;
    }

    /** Get the vocabulary id.
     * @return The vocabulary id
     */
    @Column(name = "vocabulary_id")
    public Integer getVocabularyId() {
        return vocabularyId;
    }

    /** Set the vocabulary id.
     * @param aVocabularyId the vocabulary id
     */
    public void setVocabularyId(final Integer aVocabularyId) {
        vocabularyId = aVocabularyId;
    }

    /** Get the version id.
     * @return The version id
     */
    @Column(name = "version_id")
    public Integer getVersionId() {
        return versionId;
    }

    /** Set the version id.
     * @param aVersionId the version id
     */
    public void setVersionId(final Integer aVersionId) {
        versionId = aVersionId;
    }

}
