/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.DBContext;
import ch.qos.logback.classic.Level;

/** Support program as part of the redirection of SPARQL and SISSVoc
 *  URLs containing "current" as the version identifier. Use this
 *  as a RewriteMap of type "prg" in Apache HTTP server. */
public final class RedirectCurrent {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for utility class. */
    private RedirectCurrent() {
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
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
        // The value specified as a parameter to getLogger() must be
        // specific enough to cover any settings in logback.xml.
        // The casting is done to enable the subsequent call to setLevel().
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger)
                org.slf4j.LoggerFactory.getLogger(
                        "au.org.ands.vocabs");
        rootLogger.setLevel(Level.ERROR);

        // Do the same for Hibernate. NB: May be specific to
        // the version of Hibernate, and the way we use it
        // (i.e., with Logback).
        ch.qos.logback.classic.Logger hibLogger =
                (ch.qos.logback.classic.Logger)
                org.slf4j.LoggerFactory.getLogger(
                        "org.hibernate");
        hibLogger.setLevel(Level.ERROR);

        // This initializes the database connection using the properties
        // defined in the toolkit.properties file.
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
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
                }
                System.out.println(rewrittenSlug);
                System.out.flush();
                vocabularySlug = input.readLine();
            }
        } catch (SQLException | IOException e) {
            LOGGER.error("Exception in RedirectCurrent", e);
        }
        em.getTransaction().rollback();
        em.close();
    }

}
