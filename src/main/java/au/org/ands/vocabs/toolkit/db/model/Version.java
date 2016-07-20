/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.db.TaskUtils;

/**
 * Version model class.
 */
@Entity
@Table(name = "versions")
/* Rather than including the text of the query directly in the
 * annotation, we use a constant defined in the class itself.
 * This way, it can be found in the generated Javadoc
 * in the "Constant Field Values" page. */
@NamedQuery(
        name = Version.GET_ALL_VERSIONS,
        query = Version.GET_ALL_VERSIONS_QUERY)
public class Version {

    /** Name of getAllVersions query. */
    public static final String GET_ALL_VERSIONS = "getAllVersions";
    /** Queryof getAllVersions query. */
    protected static final String GET_ALL_VERSIONS_QUERY =
            "SELECT v FROM Version v";

    /** id. */
    private Integer id;
    /** title. */
    private String title;
    /** status. */
    private String status;
    /** vocabId. */
    private Integer vocabId;
    /** data. */
    private String data;

    /** Key of the release date used in the data field. */
    private static final String RELEASE_DATE_KEY = "release_date";

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

    /** Get the release date. This value is stored in the data field. Note
     * the Transient annotation, as this is not a column in the table.
     * @return The release date
     */
    @Transient
    public String getReleaseDate() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(data);
        JsonNode releaseDate = dataJson.get(RELEASE_DATE_KEY);
        if (releaseDate == null) {
            return null;
        }
        return releaseDate.asText();
    }

    // Uncomment and test the following when needed. It has not yet
    // been tested!
//    /** Set the release date. This value is stored in the data field.
//     * Note the Transient annotation, as this is not a column in the table.
//     * @param aReleaseDate the release date
//     */
//    @Transient
//    public void setReleaseDate(final String aReleaseDate) {
//        if (data == null || data.isEmpty()) {
//            data = "{}";
//        }
//        JsonNode dataJson = TasksUtils.jsonStringToTree(data);
//        JsonObjectBuilder jobData = Json.createObjectBuilder();
//        Iterator<Entry<String, JsonNode>> dataJsonIterator =
//                dataJson.fields();
//        while (dataJsonIterator.hasNext()) {
//            Entry<String, JsonNode> entry = dataJsonIterator.next();
//            jobData.add(entry.getKey(), entry.getValue().asText());
//        }
//        jobData.add(RELEASE_DATE_KEY, aReleaseDate);
//        data = jobData.build().toString();
//    }

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

}
