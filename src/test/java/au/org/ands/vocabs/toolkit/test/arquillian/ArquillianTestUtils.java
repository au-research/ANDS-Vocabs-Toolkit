/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.HibernateException;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.FileAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.org.ands.vocabs.toolkit.db.DBContext;
import au.org.ands.vocabs.toolkit.test.utils.NetClientUtils;

/** Support methods for testing with Arquillian. */
public final class ArquillianTestUtils {

    /** Logger. */
    private static Logger logger;

    /** The {@link ClassLoader} of this class. Used for invoking
     * {@link ClassLoader#getResourceAsStream(String)}. */
    private static ClassLoader classLoader;

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        classLoader = MethodHandles.lookup().lookupClass().
                getClassLoader();
    }

    /** Private constructor for a utility class. */
    private ArquillianTestUtils() {
    }

    /** Get an {@link InputStream} for a file, given its filename.
     * @param filename The filename of the resource.
     * @return An {@link InputStream} for the resource.
     */
    private static InputStream getResourceAsInputStream(
            final String filename) {
        InputStream inputStream = classLoader.getResourceAsStream(filename);
        if (inputStream == null) {
            throw new IllegalArgumentException("Can't load resource: "
                    + filename);
        }
        return inputStream;
    }

    /** Clear the database.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void clearDatabase() throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        EntityManager em = DBContext.getEntityManager();
        try (Connection conn = em.unwrap(SessionImpl.class).connection()) {

            IDatabaseConnection connection = new H2Connection(conn, null);

            logger.info("doing clean_insert");
            FlatXmlDataSet dataset = new FlatXmlDataSetBuilder()
                    .setMetaDataSetFromDtd(getResourceAsInputStream(
                            "test/dbunit-toolkit-export-choice.dtd"))
                    .build(getResourceAsInputStream("test/blank-dbunit.xml"));

            DatabaseOperation.DELETE_ALL.execute(connection, dataset);
            // Force commit at the JDBC level, as closing the EntityManager
            // does a rollback!
            conn.commit();
        }
        em.close();
    }

    /** Load a DbUnit test file into the database.
     * The file is loaded as a {@code FlatXmlDataSet}.
     * To make it more convenient to enter JSON data, the dataset is
     * wrapped as a {@code ReplacementDataSet}, and all instances
     * of '' (two contiguous apostrophes) are replaced with "
     * (one double quote).
     * @param testName The name of the test method. Used to generate
     *      the filename of the file to load.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void loadDbUnitTestFile(final String testName) throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        EntityManager em = DBContext.getEntityManager();
        try (Connection conn = em.unwrap(SessionImpl.class).connection()) {

            IDatabaseConnection connection = new H2Connection(conn, null);

            FlatXmlDataSet xmlDataset = new FlatXmlDataSetBuilder()
                    .setMetaDataSetFromDtd(getResourceAsInputStream(
                            "test/dbunit-toolkit-export-choice.dtd"))
                    .build(getResourceAsInputStream(
                            "test/tests/au.org.ands.vocabs.toolkit."
                            + "test.arquillian.AllArquillianTests."
                            + testName
                            + "/input-dbunit.xml"));
            ReplacementDataSet dataset = new ReplacementDataSet(xmlDataset);
            dataset.addReplacementSubstring("''", "\"");
            logger.info("doing clean_insert");
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
            // Force commit at the JDBC level, as closing the EntityManager
            // does a rollback!
            conn.commit();
        }
        em.close();
    }

    /** Do a full export of the database in DBUnit format.
     * @param exportFilename The name of the file into which the
     *      export is to go.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem writing the export.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void exportFullDBData(final String exportFilename) throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        EntityManager em = DBContext.getEntityManager();
        try (Connection conn = em.unwrap(SessionImpl.class).connection()) {
            IDatabaseConnection connection = new H2Connection(conn, null);
            IDataSet fullDataSet = connection.createDataSet();
            FlatXmlDataSet.write(fullDataSet,
                    new FileOutputStream(exportFilename));
        }
        em.close();
    }

    /** Client-side clearing of the database.
     * @param baseURL The base URL to use to connect to the Toolkit.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws IOException If a problem getting test data for DBUnit.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void clientClearDatabase(final URL baseURL) throws
        DatabaseUnitException, IOException, SQLException {
        logger.info("In clientClearDatabase()");
        Response response = NetClientUtils.doGet(baseURL,
                "testing/clearDB");

        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.SUCCESSFUL,
                "clientClearDatabase response status");
        response.close();
    }

    /** Compare two files containing JSON, asserting that they contain
     * the same content.
     * @param testFilename The filename of the file containing the generated
     *      content. An TestNG assertion is made that this file exists.
     * @param correctFilename The filename of the file containing the correct
     *      value.
     * @throws IOException If reading either file fails.
     */
    public static void compareJson(final String testFilename,
            final String correctFilename) throws IOException {
        File testFile = new File(testFilename);
        FileAssert.assertFile(testFile,
                "Test file (" + testFilename + ") is not "
                        + "a proper file");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson;
        JsonNode correctJson;
        // IOException not caught here, but allowed to propagate.
        testJson = mapper.readTree(new File(testFilename));
        correctJson = mapper.readTree(new File(correctFilename));
        Assert.assertEquals(testJson, correctJson);
        // NB This uses a top-level equality test done by TestNG.
        // There is also a top-level equality test implemented by Jackson:
        // correctJson.equals(testJson). The TestNG one seems to give
        // the same end result, but gives better diagnostics in case
        // a difference is found.
    }

}
