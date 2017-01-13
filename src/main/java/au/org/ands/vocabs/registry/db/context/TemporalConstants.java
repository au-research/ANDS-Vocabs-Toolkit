/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.context;

import java.time.LocalDateTime;

/** Constants for temporal data.
 */
public final class TemporalConstants {

    /** Private constructor for a utility class. */
    private TemporalConstants() {
    }

    /** The end date of entities that are currently valid. */
    public static final LocalDateTime CURRENTLY_VALID_END_DATE =
            LocalDateTime.of(9999, 12, 1, 0, 0);

    /** The start date of entities that are draft data, representing
     * either an addition, a modification, or a deletion. */
    public static final LocalDateTime DRAFT_START_DATE =
            LocalDateTime.of(9999, 12, 2, 0, 0);

    /** The end date of entities that are draft data, representing
     * an addition or a modification. */
    public static final LocalDateTime DRAFT_ADDITION_MODIFICATION_END_DATE =
            LocalDateTime.of(9999, 12, 3, 0, 0);

    /** The end date of entities that are draft data, representing
     * a deletion.. */
    public static final LocalDateTime DRAFT_DELETION_END_DATE =
            LocalDateTime.of(9999, 12, 4, 0, 0);

}
