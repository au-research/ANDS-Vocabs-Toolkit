/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/** Todo model class. */
@Entity
public class Todo {

    /** The id for this model class. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Todo summary. */
    private String summary;
    /** Todo description. */
    private String description;

    /** Getter for summary.
     * @return The summary
     */
    public final String getSummary() {
        return summary;
    }
    /** Setter for summary.
     * @param aSummary The summary
     */
    public final void setSummary(final String aSummary) {
        this.summary = aSummary;
    }

    /** Getter for description.
     * @return The description
     */
    public final String getDescription() {
        return description;
    }

    /** Setter for description.
     * @param aDescription The description
     */
    public final void setDescription(final String aDescription) {
        this.description = aDescription;
    }

    @Override
    public final String toString() {
        return "Todo [summary=" + summary + ", description=" + description
                + "]";
    }

}
