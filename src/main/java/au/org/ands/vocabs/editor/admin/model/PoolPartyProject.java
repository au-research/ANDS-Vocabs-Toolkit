/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.editor.admin.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Details of one PoolParty project.
 *  This class has been copied from the ANDS-Vocabs-Editor-Admin codebase.
 */
@XmlRootElement
/* This annotation means that properties other than the ones defined
 * here are ignored during parsing. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PoolPartyProject implements Comparable<PoolPartyProject>,
    Serializable {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 2522160638005424651L;

    /** The project id. */
    private String id;

    /** The project title. */
    private String title;

    /** The project URI. */
    private String uri;

    /** The project URI supplement. */
    private String uriSupplement;

    /** Get the id.
     * @return The id.
     */
    public final String getId() {
        return id;
    }

    /** Set the project id.
     * @param anId The id to set.
     */
    public final void setId(final String anId) {
        id = anId;
    }

    /** Get the project title.
     * @return The title.
     */
    public final String getTitle() {
        return title;
    }

    /** Set the project title.
     * @param aTitle The title to set.
     */
    public final void setTitle(final String aTitle) {
        title = aTitle;
    }

    /** Get the project URI.
     * @return The URI.
     */
    public final String getUri() {
        return uri;
    }

    /** Set the project URI.
     * @param aUri The URI to set.
     */
    public final void setUri(final String aUri) {
        uri = aUri;
    }

    /** Get the project URI supplement.
     * @return The URI supplement.
     */
    public final String getUriSupplement() {
        return uriSupplement;
    }

    /** Set the project URI supplement.
     * @param aUriSupplement The URI supplement to set.
     */
    public final void setUriSupplement(final String aUriSupplement) {
        uriSupplement = aUriSupplement;
    }

    /** Ordering is by title. */
    @Override
    public final int compareTo(final PoolPartyProject otherProject) {
        return title.compareTo(otherProject.getTitle());
    }
}
