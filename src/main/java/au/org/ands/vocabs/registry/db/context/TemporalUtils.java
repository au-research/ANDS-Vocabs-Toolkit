/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.context;

import java.time.LocalDateTime;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

/** Support for temporal data.
 */
public final class TemporalUtils {

    /** Private constructor for a utility class. */
    private TemporalUtils() {
    }

    /** The name of the date/time parameter to use in queries. */
    private static final String DATETIME_PARAMETER = "datetime";

    /** The name of a JPQL parameter to use for the constant
     * {@link TemporalConstants#CURRENTLY_VALID_END_DATE}. */
    private static final String CURRENTLY_VALID_END_DATE =
            "currently_valid_end_date";

    /** The name of a JPQL parameter to use for the constant
     * {@link TemporalConstants#DRAFT_START_DATE}. */
    private static final String DRAFT_START_DATE =
            "draft_start_date";

    /** Start date field name. Must match the property name in the entity
     * class. */
    public static final String START_DATE = "startDate";

    /** End date field name. Must match the property name in the entity
     * class. */
    public static final String END_DATE = "endDate";

    /** Suffix for JPQL queries to select only currently-valid rows.
     * This version of the suffix is for queries that already
     * have a WHERE clause. */
    public static final String AND_TEMPORAL_QUERY_VALID_SUFFIX =
            " AND " + END_DATE + " = :" + CURRENTLY_VALID_END_DATE;
    // Original version follows. Less efficient than the previous.
//    public static final String AND_TEMPORAL_QUERY_VALID_SUFFIX =
//            " AND start_date <= :" + DATETIME_PARAMETER
//            + " AND :" + DATETIME_PARAMETER + " < end_date";

    /** Suffix for JPQL queries to select only currently-valid rows.
     * This version of the suffix is for queries that do not already
     * have a WHERE clause. */
    public static final String WHERE_TEMPORAL_QUERY_VALID_SUFFIX =
            " WHERE " + END_DATE + " = :" + CURRENTLY_VALID_END_DATE;
    // Original version follows. Less efficient than the previous.
//    public static final String WHERE_TEMPORAL_QUERY_VALID_SUFFIX =
//            " WHERE start_date <= :" + DATETIME_PARAMETER
//            + " AND :" + DATETIME_PARAMETER + " < end_date";

    /** Suffix for JPQL queries to select only draft rows.
     * This version of the suffix is for queries that already
     * have a WHERE clause. */
    public static final String AND_TEMPORAL_QUERY_ALL_DRAFT_SUFFIX =
            " AND " + START_DATE + " = :" + DRAFT_START_DATE;

    /** Suffix for JPQL queries to select only draft rows.
     * This version of the suffix is for queries that do not already
     * have a WHERE clause. */
    public static final String WHERE_TEMPORAL_QUERY_ALL_DRAFT_SUFFIX =
            " WHERE " + START_DATE + " = :" + DRAFT_START_DATE;

    /** Entity name E1, for use in JPQL queries. */
    public static final String E1 = "e1";

    /** Entity name E2, for use in JPQL queries. */
    public static final String E2 = "e2";

    /** Entity name E3, for use in JPQL queries. */
    public static final String E3 = "e3";

    /** Suffix template for JPQL queries to select only currently-valid rows
     * of entity {@link #E1}. */
    public static final String
        TEMPORAL_QUERY_TEMPLATE_VALID_SUFFIX_E1 =
            " AND " + E1 + "." + END_DATE
            + " = :" + CURRENTLY_VALID_END_DATE;
            // This is what we would _like_ to do, but it doesn't currently
            // work with Hibernate. Hibernate serializes the Timestamp
            // literal using TimestampType.objectToSQLString(), which uses
            // Timestamp.toString(). It should be like the code in
            // LocalDateTimeType.objectToSQLString(), which generates
            // a JDBC escape "{ts '9999-12-01 00:00:00.0'}".
            /*
            " AND " + E1 + "." + END_DATE
            + " = au.org.ands.vocabs.registry.db.context.TemporalConstants."
            + "CURRENTLY_VALID_END_DATE";
            */
            // This is a more "formal" way, but not necessary.
            // We don't have any endDate values that are both
            // (a) in the future, (b) less than CURRENTLY_VALID_END_DATE.
            /*
            " AND " + E1 + "." + START_DATE
            + " <= :" + DATETIME_PARAMETER + " AND :"
            + DATETIME_PARAMETER + " < " + E1 + "." + END_DATE;
            */

    // When these are needed, uncomment and make match the
    // way we do TEMPORAL_QUERY_TEMPLATE_VALID_SUFFIX_E1.
    // (Definitions below are "legacy" from the Toolkit version of this class.


//    /** Suffix template for JPQL queries to select only currently-valid rows
//     * of entity {@link #E2}. */
//    public static final String
//        TEMPORAL_QUERY_TEMPLATE_VALID_SUFFIX_E2 =
//            " AND " + E2 + "." + START_DATE
//            + " <= :" + DATETIME_PARAMETER + " AND :"
//            + DATETIME_PARAMETER + " < " + E2 + "." + END_DATE;
//
//    /** Suffix template for JPQL queries to select only currently-valid rows
//     * of entity {@link #E3}. */
//    public static final String
//        TEMPORAL_QUERY_TEMPLATE_VALID_SUFFIX_E3 =
//            " AND " + E3 + "." + START_DATE
//            + " <= :" + DATETIME_PARAMETER + " AND :"
//            + DATETIME_PARAMETER + " < " + E3 + "." + END_DATE;

    // The following are commented out for now, as not yet needed.

//    /** Get the current time in UTC as a LocalDateTime.
//     * @return The current time in UTC as a LocalDateTime value.
//     */
//    private static LocalDateTime nowUTC() {
//        return LocalDateTime.now(Clock.systemUTC());
//    }

//    /** Set the datetime parameter of a query to the current
//     * date/time.
//     * @param q The query to be modified.
//     * @return The modified query.
//     */
//    public static Query setDatetimeParameterNow(final Query q) {
//        return q.setParameter(DATETIME_PARAMETER, nowUTC());
//    }
//
//    /** Set the datetime parameter of a query to the current
//     * date/time.
//     * @param <T> The return type of the TypedQuery.
//     * @param q The query to be modified.
//     * @return The modified query.
//     */
//    public static <T> TypedQuery<T> setDatetimeParameterNow(
//            final TypedQuery<T> q) {
//        return q.setParameter(DATETIME_PARAMETER, nowUTC());
//    }

    /** Set any datetime constant parameters of a query to the
     * correct values.
     * @param <T> The return type of the TypedQuery.
     * @param q The query to be modified.
     * @return The modified query.
     */
    public static <T> TypedQuery<T> setDatetimeConstantParameters(
            final TypedQuery<T> q) {
        try {
            q.setParameter(CURRENTLY_VALID_END_DATE,
                    TemporalConstants.CURRENTLY_VALID_END_DATE);
        } catch (IllegalArgumentException e) {
            // No problem.
        }
        try {
            q.setParameter(DRAFT_START_DATE,
                    TemporalConstants.DRAFT_START_DATE);
        } catch (IllegalArgumentException e) {
            // No problem.
        }
        return q;
    }

    /** Set the datetime parameter of a query to a specified
     * date/time.
     * @param q The query to be modified.
     * @param dateTime The date/time value to be used in the query.
     * @return The modified query.
     */
    public static Query setDatetimeParameter(final Query q,
            final LocalDateTime dateTime) {
        return q.setParameter(DATETIME_PARAMETER, dateTime);
    }

    /** Set the datetime parameter of a query to a specified
     * date/time.
     * @param <T> The return type of the TypedQuery.
     * @param q The query to be modified.
     * @param dateTime The date/time value to be used in the query.
     * @return The modified query.
     */
    public static <T> TypedQuery<T> setCurrentDatetimeParameter(
            final TypedQuery<T> q, final LocalDateTime dateTime) {
        return q.setParameter(DATETIME_PARAMETER, dateTime);
    }


}
