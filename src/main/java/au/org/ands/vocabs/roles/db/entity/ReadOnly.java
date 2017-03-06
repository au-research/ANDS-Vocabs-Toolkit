/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.roles.db.entity;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/** Entity listener class to prevent updates to the roles database.
 */
public class ReadOnly {

    /** Flag that determines whether or not the entities are to be
     * treated as read-only.
     */
    private static boolean readOnly = true;

    /** Get the current value of the readOnly flag.
     * @return The current value of the readOnly flag.
     */
    public static boolean getReadOnly() {
        return readOnly;
    }

    /** Set the value of the readOnly flag.
     * @param newReadOnly The new value of the readOnly flag.
     */
    public static void setReadOnly(final boolean newReadOnly) {
        readOnly = newReadOnly;
    }

    /** Entity listener method for {@link PrePersist},
     * {@link PreRemove}, and {@link PreUpdate}.
     * Throws a {@link RuntimeException} if the {@link readOnly} flag
     * is set.
     * @param o The object attempting to be modified.
     */
    @PrePersist
    @PreRemove
    @PreUpdate
    void onPreModification(final Object o) {
        if (readOnly) {
            throw new RuntimeException("Denied an attempt to modify "
                    + "read-only entity: " + o);
        }
    }

}
