/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.db;

import java.time.Clock;
import java.time.LocalDateTime;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

/** Support for temporal data.
 */
public final class TemporalUtils {

    /** Private constructor for a utility class. */
    private TemporalUtils() {
    }

    /** The name of the current date/time parameter to use in queries. */
    private static final String CURRENT_DATETIME = "current_datetime";

    /** Suffix for JPQL queries to select only currently-valid rows. */
    public static final String TEMPORAL_QUERY_CURRENTLY_VALID_SUFFIX =
            " AND start_date <= :" + CURRENT_DATETIME
            + " AND :" + CURRENT_DATETIME + " < end_date";

    /** Entity name E1, for use in JPQL queries. */
    public static final String E1 = "e1";

    /** Entity name E2, for use in JPQL queries. */
    public static final String E2 = "e2";

    /** Entity name E3, for use in JPQL queries. */
    public static final String E3 = "e3";

    /** Start date field name. Must match the property name in the entity
     * class.*/
    public static final String START_DATE = "startDate";

    /** End date field name. Must match the property name in the entity
     * class.*/
    public static final String END_DATE = "endDate";

    /** Suffix template for JPQL queries to select only currently-valid rows
     * of entity {@link #E1}. */
    public static final String
        TEMPORAL_QUERY_TEMPLATE_CURRENTLY_VALID_SUFFIX_E1 =
            " AND " + E1 + "." + START_DATE
            + " <= :" + CURRENT_DATETIME + " AND :"
            + CURRENT_DATETIME + " < " + E1 + "." + END_DATE;

    /** Suffix template for JPQL queries to select only currently-valid rows
     * of entity {@link #E2}. */
    public static final String
        TEMPORAL_QUERY_TEMPLATE_CURRENTLY_VALID_SUFFIX_E2 =
            " AND " + E2 + "." + START_DATE
            + " <= :" + CURRENT_DATETIME + " AND :"
            + CURRENT_DATETIME + " < " + E2 + "." + END_DATE;

    /** Suffix template for JPQL queries to select only currently-valid rows
     * of entity {@link #E3}. */
    public static final String
        TEMPORAL_QUERY_TEMPLATE_CURRENTLY_VALID_SUFFIX_E3 =
            " AND " + E3 + "." + START_DATE
            + " <= :" + CURRENT_DATETIME + " AND :"
            + CURRENT_DATETIME + " < " + E3 + "." + END_DATE;

    /** Get the current time in UTC as a LocalDateTime.
     * @return The current time in UTC as a LocalDateTime value.
     */
    private static LocalDateTime nowUTC() {
        return LocalDateTime.now(Clock.systemUTC());
    }

    /** Set the current datetime parameter of a query to the current
     * date/time.
     * @param q The query to be modified.
     * @return The modified query.
     */
    public static Query setCurrentDatetimeParameterNow(final Query q) {
        return q.setParameter(CURRENT_DATETIME, nowUTC());
    }

    /** Set the current datetime parameter of a query to the current
     * date/time.
     * @param <T> The return type of the TypedQuery.
     * @param q The query to be modified.
     * @return The modified query.
     */
    public static <T> TypedQuery<T> setCurrentDatetimeParameterNow(
            final TypedQuery<T> q) {
        return q.setParameter(CURRENT_DATETIME, nowUTC());
    }

    /** Set the current datetime parameter of a query to a specified
     * date/time.
     * @param q The query to be modified.
     * @param dateTime The date/time value to be used in the query.
     * @return The modified query.
     */
    public static Query setCurrentDatetimeParameterNow(final Query q,
            final LocalDateTime dateTime) {
        return q.setParameter(CURRENT_DATETIME, dateTime);
    }

    /** Set the current datetime parameter of a query to a specified
     * date/time.
     * @param <T> The return type of the TypedQuery.
     * @param q The query to be modified.
     * @param dateTime The date/time value to be used in the query.
     * @return The modified query.
     */
    public static <T> TypedQuery<T> setCurrentDatetimeParameterNow(
            final TypedQuery<T> q, final LocalDateTime dateTime) {
        return q.setParameter(CURRENT_DATETIME, dateTime);
    }


}
