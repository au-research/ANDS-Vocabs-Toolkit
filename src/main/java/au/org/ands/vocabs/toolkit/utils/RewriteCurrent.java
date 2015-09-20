/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.DBContext;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

/** Support program as part of the redirection of SPARQL and SISSVoc
 *  URLs containing "current" as the version identifier. Use this
 *  as a RewriteMap of type "prg" in Apache HTTP server. */
public final class RewriteCurrent {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for utility class. */
    private RewriteCurrent() {
    }

    /** Query string for accessing the current version. */
    private static final String QUERY_STRING =
            "SELECT ver.title from vocabularies voc, versions ver "
            + "WHERE voc.slug= ? "
            + "AND voc.status='published' and ver.vocab_id = voc.id "
            + "AND ver.status='current'";

    /** Main program. Reads lines from standard input, until EOF reached.
     * For each line read, treat that as a vocabulary slug, and look
     * that up in the database to find the title of the current version,
     * if there is one, and as long as it has been published.
     * Output the slug form of that version title.
     * If there is no such current version, output the string "NULL"
     * as required by the Apache HTTP Server RewriteMap.
     * NB, this relies on the uniqueness of slugs. Won't work if we
     * allow multiple owners with the same slug.
     * Logging absolutely must be set up so as not to display any
     * logging on standard output (as would happen by default for Hibernate).
     * Specify a custom logback.xml with -Dlogback.configurationFile=...
     * on the command-line to do this.
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
        // This initializes the database connection using the properties
        // defined in the toolkit.properties file.
        EntityManager em = DBContext.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        // The following as a courtesy, but probably not needed.
        trans.setRollbackOnly();
        try {
            // Get the raw JDBC connection, for efficiency.
            // NB: This is Hibernate specific!
            Connection conn = em.unwrap(SessionImpl.class).connection();
            PreparedStatement stmt = conn.prepareStatement(QUERY_STRING);
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(System.in));
            String vocabularySlug;
            String rewrittenSlug;
            vocabularySlug = input.readLine();
            while (vocabularySlug != null && vocabularySlug.length() != 0) {
                stmt.setString(1, vocabularySlug);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        rewrittenSlug =
                                ToolkitFileUtils.makeSlug(rs.getString(1));
                    } else {
                        // No answer.
                        rewrittenSlug = "NULL";
                    }
                } catch (CommunicationsException e) {
                    // Handle the specific case of the Connection object
                    // timing out. Close down, and try once more.
                    // If an exception happens here, the program will exit.
                    // Oops? Good enough for now, unless/until we observe
                    // any other type of behaviour.
                    stmt.close();
                    em.close();
                    em = DBContext.getEntityManager();
                    trans = em.getTransaction();
                    trans.begin();
                    trans.setRollbackOnly();
                    conn = em.unwrap(SessionImpl.class).connection();
                    stmt = conn.prepareStatement(QUERY_STRING);
                    stmt.setString(1, vocabularySlug);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            rewrittenSlug =
                                    ToolkitFileUtils.makeSlug(rs.getString(1));
                        } else {
                            // No answer.
                            rewrittenSlug = "NULL";
                        }
                    }
                }
                System.out.println(rewrittenSlug);
                System.out.flush();
                vocabularySlug = input.readLine();
            }
        } catch (Exception e) {
            // Catch-all, because we don't know what else might happen
            // in future, and we want a record of that if/when
            // we get a type of exception we haven't seen before.
            LOGGER.error("Exception in RewriteCurrent", e);
        }
        // Tidy up as a courtesy ... although, rollback() will throw
        // an exception if the connection has timed out.
        em.getTransaction().rollback();
        em.close();
    }

}
