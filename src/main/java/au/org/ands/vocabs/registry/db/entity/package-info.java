/** See the file "LICENSE" for the full license governing this code. */
/** <p>Registry database model classes.</p>
 * <p>The source code of this package
 * is generated from the specification of the database tables for
 * Liquibase.</p>
 *
 * <p>Each entity class defines at least one named query to fetch
 * instances of the entity. For an entity class {@code Xyz}, that
 * named query is named {@code GET_ALL_XYZ}.</p>
 *
 * <p>The named query {@code GET_ALL_XYZ} always fetches all rows of the
 * underlying database table.</p>
 *
 * <p>If the entity class has {@code startDate} and {@code endDate}
 * attributes (i.e., because the underlying database table has
 * {@code start_date} and {@code end_date} columns), there is also
 * a named query {@code GET_ALL_TIMED_XYZ}.
 *
 * <p>The named query {@code GET_ALL_TIMED_XYZ} fetches all
 * instances which are valid at a particular date/time, i.e., for which
 * the particular date/time value lies between the instance's
 * {@code startDate} and {@code endDate} values. The particular
 * date/time is specified in the query text as a named parameter.</p>
 *
 * <p>In general, then, the {@code GET_ALL_XYZ} named queries are for
 * "low-level" operations on the database, and the {@code GET_ALL_TIMED_XYZ}
 * queries are for general use, e.g., in the implementation of API calls
 * and in the implementation of the portal, as well as to support
 * "time travel" operations.</p>
 *  */
package au.org.ands.vocabs.registry.db.entity;
