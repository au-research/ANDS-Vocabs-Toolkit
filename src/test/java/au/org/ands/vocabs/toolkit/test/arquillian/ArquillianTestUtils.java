/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatDtdWriter;
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

    /** Clear the database. Tables are truncated, and sequence values
     * for auto-incrementing columns are reset.
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DbUnit.
     * @throws SQLException If DbUnit has a problem performing
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

            // Delete the contents of the tables referred to in
            // the dataset.
            DatabaseOperation.DELETE_ALL.execute(connection, dataset);

            // Now reset the sequences used for auto-increment columns.
            // As DbUnit does not provide direct support for this,
            // this is H2-specific!
            // Inspired by: http://stackoverflow.com/questions/8523423
            // Get the names of the tables referred to in the dataset
            // used for blanking ...
            String[] tableNames = dataset.getTableNames();
            // ... and convert that into a string of the form:
            // "'TABLE_1', 'TABLE_2', 'TABLE_3'"
            String tableNamesForQuery =
                    Arrays.asList(tableNames).stream()
                    .map(i -> "'" + i.toString() + "'")
                    .collect(Collectors.joining(", "));

            // This query gets the names of the sequences used by
            // the tables in the dataset.
            String getSequencesQuery =
                    "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                    + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME IN ("
                    + tableNamesForQuery
                    + ") AND SEQUENCE_NAME IS NOT NULL";

            Set<String> sequences = new HashSet<String>();
            try (Statement s = conn.createStatement();
                    ResultSet rs = s.executeQuery(getSequencesQuery)) {
                while (rs.next()) {
                    sequences.add(rs.getString(1));
                }
                for (String seq : sequences) {
                    s.executeUpdate("ALTER SEQUENCE "
                            + seq + " RESTART WITH 1");
                }
            }
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
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DbUnit.
     * @throws SQLException If DbUnit has a problem performing
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

    /** Load a DbUnit test file into the database as an update.
     * The file is loaded as a {@code FlatXmlDataSet}.
     * To make it more convenient to enter JSON data, the dataset is
     * wrapped as a {@code ReplacementDataSet}, and all instances
     * of '' (two contiguous apostrophes) are replaced with "
     * (one double quote).
     * The data is loaded in as an update.
     * @param testName The name of the test method. Used to generate
     *      the path to the file to load.
     * @param filename The name of the file to be loaded.
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DbUnit.
     * @throws SQLException If DbUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void loadDbUnitTestFileAsUpdate(final String testName,
            final String filename) throws
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
                            + "/" + filename));
            ReplacementDataSet dataset = new ReplacementDataSet(xmlDataset);
            dataset.addReplacementSubstring("''", "\"");
            logger.info("doing update");
            DatabaseOperation.UPDATE.execute(connection, dataset);
            // Force commit at the JDBC level, as closing the EntityManager
            // does a rollback!
            conn.commit();
        }
        em.close();
    }

    /** Export the DbUnit database schema as a DTD.
     * @param dtdExportFilename The name of the file into which the
     *      DTD export is to go.
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws SQLException If DbUnit has a problem performing
     *           performing JDBC operations.
     * @throws IOException If a problem opening or closing the output file.
     */
    public static void exportDbUnitDTD(final String dtdExportFilename) throws
        DatabaseUnitException, SQLException, IOException {
        EntityManager em = DBContext.getEntityManager();
        try (Connection conn = em.unwrap(SessionImpl.class).connection()) {

            IDatabaseConnection connection = new H2Connection(conn, null);
            IDataSet dataSet = connection.createDataSet();
            Writer out =
                    new OutputStreamWriter(new FileOutputStream(
                            dtdExportFilename), StandardCharsets.UTF_8);
            FlatDtdWriter datasetWriter = new FlatDtdWriter(out);
            datasetWriter.setContentModel(FlatDtdWriter.CHOICE);
            // You could also use the sequence model, which is the default:
            // datasetWriter.setContentModel(FlatDtdWriter.SEQUENCE);
            datasetWriter.write(dataSet);
            out.close();
        }
        em.close();
    }

    /** Do a full export of the database in DbUnit format.
     * @param exportFilename The name of the file into which the
     *      export is to go.
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem writing the export.
     * @throws SQLException If DbUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void exportFullDbUnitData(final String exportFilename) throws
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
     */
    public static void clientClearDatabase(final URL baseURL) {
        logger.info("In clientClearDatabase()");
        Response response = NetClientUtils.doGet(baseURL,
                "testing/clearDB");

        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.SUCCESSFUL,
                "clientClearDatabase response status");
        response.close();
    }

    /** Client-side loading of the database.
     * @param baseURL The base URL to use to connect to the Toolkit.
     * @param testName The name of the test method. Used to generate
     *      the filename of the file to load.
     */
    public static void clientLoadDbUnitTestFile(final URL baseURL,
            final String testName) {
        logger.info("In clientLoadDbUnitTestFile()");
        Response response = NetClientUtils.doGetWithAdditionalComponents(
                baseURL, "testing/loadDB",
                webTarget -> webTarget.queryParam("testName", testName));

        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.SUCCESSFUL,
                "clientLoadDbUnitTestFile response status");
        response.close();
    }

    /** Client-side loading of the database as an update.
     * @param baseURL The base URL to use to connect to the Toolkit.
     * @param testName The name of the test method. Used to generate
     *      the path to the file to load.
     * @param filename The name of the file to be loaded.
     */
    public static void clientLoadDbUnitTestFileAsUpdate(final URL baseURL,
            final String testName, final String filename) {
        logger.info("In clientLoadDbUnitTestFileAsUpdate()");
        Response response = NetClientUtils.doGetWithAdditionalComponents(
                baseURL, "testing/loadDBAsUpdate",
                webTarget ->
                    webTarget.queryParam("testName", testName)
                    .queryParam("filename", filename));

        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.SUCCESSFUL,
                "clientLoadDbUnitTestFileAsUpdate response status");
        response.close();
    }


    /** Get the current contents of a database table.
     * @param tableName The name of the database table to be fetched.
     * @return The current contents of the database table.
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws SQLException If DbUnit has a problem performing
     *           performing JDBC operations.
     */
    public static ITable getDatabaseTableCurrentContents(
            final String tableName) throws
            DatabaseUnitException, HibernateException, SQLException {
        EntityManager em = DBContext.getEntityManager();
        ITable currentTable;
        try (Connection conn = em.unwrap(SessionImpl.class).connection()) {
            IDatabaseConnection connection = new H2Connection(conn, null);
            IDataSet databaseDataSet = connection.createDataSet();
            currentTable = databaseDataSet.getTable(tableName);
        }
        em.close();
        return currentTable;
    }

    /** Get the contents of a dataset in a file containing expected
     * database contents.
     * @param filename The filename of the file containing the expected
     *      database contents.
     * @return The contents of the database table.
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws IOException If reading the DTD fails.
     */
    public static IDataSet getDatabaseTableExpectedContents(
            final String filename) throws
            DatabaseUnitException, IOException {
        FlatXmlDataSet xmlDataset = new FlatXmlDataSetBuilder()
                .setMetaDataSetFromDtd(getResourceAsInputStream(
                        "test/dbunit-toolkit-export-choice.dtd"))
                .build(getResourceAsInputStream(
                        filename));
        ReplacementDataSet dataset = new ReplacementDataSet(xmlDataset);
        dataset.addReplacementSubstring("''", "\"");
        return dataset;
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
