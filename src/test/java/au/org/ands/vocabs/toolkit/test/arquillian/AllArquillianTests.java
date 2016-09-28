/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.hibernate.HibernateException;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.restlet.ResolveIRI;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskRunner;
import au.org.ands.vocabs.toolkit.test.utils.NetClientUtils;
import au.org.ands.vocabs.toolkit.test.utils.TestPropertyConstants;
import au.org.ands.vocabs.toolkit.utils.ApplicationContextListener;
import au.org.ands.vocabs.toolkit.utils.ToolkitConfig;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** All Arquillian tests of the Toolkit.
 * Very unfortunately, there is no way to share Arquillian deployments
 * across multiple classes. Each separate test class causes a fresh
 * deployment. So for now, put all tests here. When Suite support
 * is implemented, refactor. See
 * <a href="https://issues.jboss.org/browse/ARQ-197">JBoss JIRA ARQ-197</a>.
 * At least we can put the deployment definition in a parent class
 * @see ArquillianBaseTest
 */
@Test(groups = "arquillian")
public class AllArquillianTests extends ArquillianBaseTest {

    /** Logger. */
    private static Logger logger;

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

    /** Embedded Tomcat for running OpenRDF Sesame. */
    private static Tomcat tomcat;

    // Test setup/shutdown

    /** Set up the suite. This means:
     * clear out the contents of the repository (deleting
     * the directory pointed to by property {@code Toolkit.storagePath}).
     * Note: Arquillian invokes this method first on the client side, and
     * then on the server side after deployment.
     * @throws IOException If unable to remove and create the working
     *      directory for the embedded Tomcat server, or to
     *      remove the repository directory
     *      {@code Toolkit.storagePath}.
     * @throws LifecycleException If unable to start the embedded Tomcat
     *      server.
     * @throws ServletException If unable to deploy the Sesame webapp
     *      to the embedded Tomcat.
     */
    @BeforeSuite(groups = "arquillian")
    public final void setupSuite() throws IOException, ServletException,
        LifecycleException {
        if (ApplicationContextListener.getServletContext() == null) {
            logger.info("In AllArquillianTests.setupSuite() on client side");
            // Start Tomcat for Sesame here. Null test, "just in case"
            // already started (but this probably can't be the case).
            if (tomcat == null) {
                final String tomcatDirectory = ToolkitProperties.getProperty(
                        TestPropertyConstants.TEST_TOMCAT_DIRECTORY);
                final File catalinaHome = new File(tomcatDirectory);
                // Always start the this Tomcat with a scratch work area.
                FileUtils.deleteQuietly(catalinaHome);
                // The webapps directory must already exist, for
                // deployment of a webapp to succeed.
                FileUtils.forceMkdir(new File(catalinaHome, "webapps"));
                // Force Sesame to store repositories here, rather
                // than elsewhere (e.g., in ~/.aduna, etc.).
                System.setProperty("info.aduna.platform.appdata.basedir",
                        (new File(tomcatDirectory,
                                "openrdf")).getAbsolutePath());
                System.setProperty("info.aduna.logging.dir",
                        (new File(tomcatDirectory,
                                "adunalogging")).getAbsolutePath());
                tomcat = new Tomcat();
                tomcat.setBaseDir(catalinaHome.getAbsolutePath());
                tomcat.setPort(Integer.parseInt(ToolkitProperties.getProperty(
                        TestPropertyConstants.TEST_TOMCAT_PORT)));
                File war = new File(ToolkitProperties.getProperty(
                        TestPropertyConstants.TEST_TOMCAT_SESAME_WAR));
                tomcat.addWebapp("/" + ToolkitProperties.getProperty(
                        TestPropertyConstants.TEST_TOMCAT_SESAME_CONTEXT),
                        war.getAbsolutePath());
                logger.info("Starting Tomcat for Sesame");
                tomcat.start();
            }
        } else {
            logger.info("In AllArquillianTests.setupSuite() on server side");
            FileUtils.deleteDirectory(new File(
                    ToolkitConfig.ROOT_FILES_PATH));
        }
    }

    /** Shut down the suite.
     * Note: Arquillian invokes this method first on the server side, and
     * then on the client side after all tests are completed.
     * @throws LifecycleException If unable to shut down the embedded Tomcat
     *      running OpenRDF Sesame.
     */
    @AfterSuite(groups = "arquillian")
    public final void shutdownSuite() throws LifecycleException {
        if (ApplicationContextListener.getServletContext() == null) {
            logger.info("In AllArquillianTests.shutdownSuite() on client side");
            // Stop Tomcat for Sesame here.
            if (tomcat != null) {
                logger.info("Stopping Tomcat for Sesame");
                tomcat.stop();
                tomcat.destroy();
            }
        } else {
            logger.info("In AllArquillianTests.shutdownSuite() on server side");
        }
    }

    // Server-side tests go here. Client-side tests later on.

    // Tests of class au.org.ands.vocabs.toolkit.db.TasksUtils.

    /** Server-side test of {@code TasksUtils.getAllTasks()}
     * when there are no tasks. */
    @Test
    public final void testGetAllTasks() {
        logger.info("In testGetAllTasks()");
        List<Task> taskList = TaskUtils.getAllTasks();
        Assert.assertNotNull(taskList,
                "getAllTasks() with no tasks");
        Assert.assertEquals(taskList.size(), 0,
                "getAllTasks() with no tasks");
    }

    // Tests of class
    // au.org.ands.vocabs.toolkit.provider.transform.JsonTreeTransformProvider.

    // Task numbers 3 and 4 generate magic number warnings.
    //CHECKSTYLE:OFF: MagicNumber
    /** Server-side test of {@code JsonTreeTransformProvider}.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit,
     *          or reading JSON from the correct and test output files.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    @Test
    public final void testJsonTreeTransformProvider1() throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        logger.info("In testJsonTreeTransformProvider1()");
        ArquillianTestUtils.clearDatabase();
        ArquillianTestUtils.loadDbUnitTestFile(
                "testJsonTreeTransformProvider1");
        List<Task> taskList = TaskUtils.getAllTasks();
        logger.info("testJsonTreeTransformProvider1: task list length = "
                + taskList.size());
        TaskInfo taskInfo = ToolkitFileUtils.getTaskInfo(1);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 1");
        TaskRunner runner = new TaskRunner(taskInfo);
        runner.runTask();
        HashMap<String, String> results = runner.getResults();

        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "JsonTreeTransformProvider failed on task 1");
        String conceptsTreeFilename = results.get("concepts_tree");
        ArquillianTestUtils.compareJson(conceptsTreeFilename,
                "src/test/resources/input/"
                + "au.org.ands.vocabs.toolkit.test.arquillian."
                + "AllArquillianTests.testJsonTreeTransformProvider1/"
                + "test-data1-concepts_tree.json");

        taskInfo = ToolkitFileUtils.getTaskInfo(2);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 2");
        runner = new TaskRunner(taskInfo);
        runner.runTask();
        results = runner.getResults();

        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "JsonTreeTransformProvider failed on task 2");
        conceptsTreeFilename = results.get("concepts_tree");
        // Note the use of the same correct output as the previous test.
        ArquillianTestUtils.compareJson(conceptsTreeFilename,
                "src/test/resources/input/"
                + "au.org.ands.vocabs.toolkit.test.arquillian."
                + "AllArquillianTests.testJsonTreeTransformProvider1/"
                + "test-data1-concepts_tree.json");

        // Polyhierarchy detection
        taskInfo = ToolkitFileUtils.getTaskInfo(3);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 3");
        runner = new TaskRunner(taskInfo);
        runner.runTask();
        results = runner.getResults();

        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "JsonTreeTransformProvider failed on task 3");
        Assert.assertFalse(results.containsKey("concepts_tree"),
                "JsonTreeTransformProvider task 3 returned a concepts_tree "
                + "value");
        Assert.assertEquals(results.get("concepts_tree_not_provided"),
                "No concepts tree provided, because there is a forward "
                + "or cross edge.",
                "JsonTreeTransformProvider task 3 returned wrong value for "
                + "concepts_tree_not_provided");

        // Cycle detection
        taskInfo = ToolkitFileUtils.getTaskInfo(4);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 4");
        runner = new TaskRunner(taskInfo);
        runner.runTask();
        results = runner.getResults();

        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "JsonTreeTransformProvider failed on task 4");
        Assert.assertFalse(results.containsKey("concepts_tree"),
                "JsonTreeTransformProvider task 4 returned a concepts_tree "
                + "value");
        Assert.assertEquals(results.get("concepts_tree_not_provided"),
                "No concepts tree provided, because there is a cycle.",
                "JsonTreeTransformProvider task 4 returned wrong value for "
                + "concepts_tree_not_provided");

    }
    //CHECKSTYLE:ON: MagicNumber

    // Tests of class
    // au.org.ands.vocabs.toolkit.
    //     provider.transform.ResourceMapTransformProvider.

    /** Server-side test 1 of {@code ResourceMapTransformProvider}.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit,
     *          or reading JSON from the correct and test output files.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    @Test
    public final void testResourceMapTransformProvider1() throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        logger.info("In testResourceMapTransformProvider1()");
        ArquillianTestUtils.clearDatabase();
        ArquillianTestUtils.loadDbUnitTestFile(
                "testResourceMapTransformProvider1");
        List<Task> taskList = TaskUtils.getAllTasks();
        logger.info("testResourceMapTransformProvider1: task list length = "
                + taskList.size());
        TaskInfo taskInfo = ToolkitFileUtils.getTaskInfo(1);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 1");
        TaskRunner runner = new TaskRunner(taskInfo);
        runner.runTask();
        HashMap<String, String> results = runner.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "ResourceMapTransformProvider failed on task 1");
        // If a dump is required, uncomment the next line.
        // ArquillianTestUtils.exportFullDBData("trmtp1.xml");

        // Get current contents of resource_map table.
        ITable actualTable = ArquillianTestUtils.
                getDatabaseTableCurrentContents("RESOURCE_MAP");
        // And take out the id and access_point_id columns before
        // doing a comparison.
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(
                actualTable, new String[]{"ID", "ACCESS_POINT_ID"});

        IDataSet expectedDataSet = ArquillianTestUtils.
                getDatabaseTableExpectedContents(
                        "test/tests/au.org.ands.vocabs.toolkit."
                        + "test.arquillian.AllArquillianTests."
                        + "testResourceMapTransformProvider1/"
                        + "test-data1-results.xml");
        ITable expectedTable = expectedDataSet.getTable("RESOURCE_MAP");
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(
                expectedTable, new String[]{"ID", "ACCESS_POINT_ID"});
        Assertion.assertEquals(new SortedTable(filteredExpectedTable),
                new SortedTable(filteredActualTable,
                        filteredExpectedTable.getTableMetaData()));

        // Now do an UNTRANSFORM.
        taskInfo = ToolkitFileUtils.getTaskInfo(2);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 2");
        runner = new TaskRunner(taskInfo);
        runner.runTask();
        results = runner.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "ResourceMapTransformProvider failed on task 2");
        // Get current contents of resource_map table again.
        actualTable = ArquillianTestUtils.
                getDatabaseTableCurrentContents("RESOURCE_MAP");
        Assert.assertEquals(actualTable.getRowCount(), 0,
                "Empty resource_map table after UNTRANSFORM");
    }

    /** Server-side test 2 of {@code ResourceMapTransformProvider}.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit,
     *          or reading JSON from the correct and test output files.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    @Test
    public final void testResourceMapTransformProvider2() throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        logger.info("In testResourceMapTransformProvider2()");
        ArquillianTestUtils.clearDatabase();
        ArquillianTestUtils.loadDbUnitTestFile(
                "testResourceMapTransformProvider2");
        List<Task> taskList = TaskUtils.getAllTasks();
        logger.info("testResourceMapTransformProvider2: task list length = "
                + taskList.size());
        TaskInfo taskInfo = ToolkitFileUtils.getTaskInfo(1);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 1");
        TaskRunner runner = new TaskRunner(taskInfo);
        runner.runTask();
        HashMap<String, String> results = runner.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "ResourceMapTransformProvider failed on task 1");
        // If a dump is required, uncomment the next line.
        // ArquillianTestUtils.exportFullDBData("trmtp2.xml");

        // Get current contents of resource_map table.
        ITable actualTable = ArquillianTestUtils.
                getDatabaseTableCurrentContents("RESOURCE_MAP");
        // And take out the id and access_point_id columns before
        // doing a comparison.
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(
                actualTable, new String[]{"ID", "ACCESS_POINT_ID"});

        IDataSet expectedDataSet = ArquillianTestUtils.
                getDatabaseTableExpectedContents(
                        "test/tests/au.org.ands.vocabs.toolkit."
                        + "test.arquillian.AllArquillianTests."
                        + "testResourceMapTransformProvider2/"
                        + "test-data1-results.xml");
        ITable expectedTable = expectedDataSet.getTable("RESOURCE_MAP");
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(
                expectedTable, new String[]{"ID", "ACCESS_POINT_ID"});
        Assertion.assertEquals(new SortedTable(filteredExpectedTable),
                new SortedTable(filteredActualTable,
                        filteredExpectedTable.getTableMetaData()));
    }

    /** Server-side test 3 of {@code ResourceMapTransformProvider}.
     * A test of resources of all SKOS types, and of deprecation,
     * where there are two vocabularies with two different owners, and
     * there is some overlap in ownership of hosts. There is also a
     * test of multiple definitions of a concept within the same
     * vocabulary.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit,
     *          or reading JSON from the correct and test output files.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    @Test
    public final void testResourceMapTransformProvider3() throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        logger.info("In testResourceMapTransformProvider3()");
        ArquillianTestUtils.clearDatabase();
        ArquillianTestUtils.loadDbUnitTestFile(
                "testResourceMapTransformProvider3");
        List<Task> taskList = TaskUtils.getAllTasks();
        logger.info("testResourceMapTransformProvider3: task list length = "
                + taskList.size());
        TaskInfo taskInfo = ToolkitFileUtils.getTaskInfo(1);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 1");
        TaskRunner runner = new TaskRunner(taskInfo);
        runner.runTask();
        HashMap<String, String> results = runner.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "ResourceMapTransformProvider failed on task 1");

        taskInfo = ToolkitFileUtils.getTaskInfo(2);
        Assert.assertNotNull(taskInfo, "Test data not loaded, task 2");
        runner = new TaskRunner(taskInfo);
        runner.runTask();
        results = runner.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals(results.get("status"), "success",
                "ResourceMapTransformProvider failed on task 2");
        // If a dump is required, uncomment the next line.
        // ArquillianTestUtils.exportFullDBData("trmtp3.xml");

        // Get current contents of resource_map table.
        ITable actualTable = ArquillianTestUtils.
                getDatabaseTableCurrentContents("RESOURCE_MAP");
        // And take out the id column before
        // doing a comparison. Cf. the other tests, in which we also
        // take out the access_point_id column. This time, we have
        // two access points in play, so we rely on the database
        // cleaning having reset the counters, so that generated ids can be
        // relied on to start from 1.
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(
                actualTable, new String[]{"ID"});

        IDataSet expectedDataSet = ArquillianTestUtils.
                getDatabaseTableExpectedContents(
                        "test/tests/au.org.ands.vocabs.toolkit."
                        + "test.arquillian.AllArquillianTests."
                        + "testResourceMapTransformProvider3/"
                        + "test-data1-results.xml");
        ITable expectedTable = expectedDataSet.getTable("RESOURCE_MAP");
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(
                expectedTable, new String[]{"ID"});
        Assertion.assertEquals(new SortedTable(filteredExpectedTable),
                new SortedTable(filteredActualTable,
                        filteredExpectedTable.getTableMetaData()));
    }

    // Client-side tests go here. Server-side tests are above this line.

    // Tests of restlets defined in
    //   au.org.ands.vocabs.toolkit.restlet.GetInfo.

    /** The base URL of the deployed webapp under test.
     * Injected by Arquillian.
     * For future ref: if instead of being specified as a private
     * field, this was to be
     * injected as a parameter into a method annotated as {@code @Test},
     * TestNG has to be made happy by saying
     * {@code @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)}.
     */
    private @ArquillianResource URL baseURL;

    /** Client-side test of the system health check function.
     */
    @Test
    @RunAsClient
    public final void testSystemHealthCheck() {
        logger.info("In testSystemHealthCheck()");
        ArquillianTestUtils.clientClearDatabase(baseURL);
        Response response = NetClientUtils.doGet(baseURL,
                "getInfo/systemHealthCheck", MediaType.APPLICATION_JSON_TYPE);

        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.SUCCESSFUL,
                "systemHealthCheck response status");
        String body = response.readEntity(String.class);
        response.close();

        Assert.assertEquals(body, "[]",
            "systemHealthCheck return value");
        // Assert.fail("Test of failing in testSystemHealthCheck");
    }

    /** Client-side test 1 of the global IRI resolver function,
     * {@link au.org.ands.vocabs.toolkit.restlet.ResolveIRI#lookupIRI}.
     * Lookup data is based on the correct result
     * of {@link #testResourceMapTransformProvider1()}.
     */
    @Test
    @RunAsClient
    public final void testResolveIRILookupIRI1() {
        logger.info("In testResolveIRILookupIRI1()");
        ArquillianTestUtils.clientClearDatabase(baseURL);
        ArquillianTestUtils.clientLoadDbUnitTestFile(baseURL,
                "testResolveIRILookupIRI1");

        String service = "resolve/lookupIRI";

        // Test: resource IRI not specified.
        Response response = NetClientUtils.doGet(baseURL,
                service);
        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.CLIENT_ERROR,
                "lookupIRI response status");
        String body = response.readEntity(String.class);
        response.close();
        Assert.assertTrue(body.startsWith(ResolveIRI.NO_IRI_SPECIFIED),
                "Error message when no IRI specified");

        // Test: unsupported mode specified.
        response = NetClientUtils.doGetWithAdditionalComponents(
                baseURL, service,
                webTarget -> webTarget.queryParam("iri", "test").
                    queryParam("mode", "bogusMode"));
        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.CLIENT_ERROR,
                "lookupIRI response status");
        body = response.readEntity(String.class);
        response.close();
        Assert.assertTrue(body.startsWith(ResolveIRI.UNSUPPORTED_MODE),
                "Error message when unsupported mode specified");

        // Test: bogus IRI specified.
        response = NetClientUtils.doGetWithAdditionalComponents(
                baseURL, service,
                webTarget -> webTarget.queryParam("iri", "test"));
        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.CLIENT_ERROR,
                "lookupIRI response status");
        body = response.readEntity(String.class);
        response.close();
        Assert.assertTrue(body.startsWith(ResolveIRI.NO_DEFINITION),
                "Error message when bogus IRI specified");

        String redirectPrefix =
                "http://testing-host.org.au/repository/api/lda/"
                + "ands/testresourcemaptransformprovider1/v1/resource";

        // Tests: valid IRI specified.
        String[] resourcesThatShouldResolve = {
                "http://vocab.owner.org/def/vocab1/1",
                "http://vocab.owner.org/def/vocab1/2",
                "http://vocab.owner.org/def/vocab1/3",
                "http://vocab.owner.org/def/vocab1/5",
                "http://vocab.owner.org/def/vocab1/6",
                "http://vocab.owner.org/def/vocab1/7",
                "http://vocab.owner.org/def/vocab1/9"
        };

        testValidIRISpecified(service, redirectPrefix,
                resourcesThatShouldResolve);

        String[] resourcesThatShouldNotResolve = {
                "http://some.other.org/def/vocab1/1",
                "http://some.other.org/def/vocab1/2",
                "http://some.other.org/def/vocab1/3",
                "http://some.other.org/def/vocab1/5",
                "http://some.other.org/def/vocab1/6",
                "http://some.other.org/def/vocab1/7",
                "http://some.other.org/def/vocab1/9"
        };

        testInvalidIRISpecified(service, resourcesThatShouldNotResolve,
                ResolveIRI.NO_DEFINITION,
                "Error message when non-owned IRI specified");
    }

    /** Client-side test 2 of the global IRI resolver function,
     * {@link au.org.ands.vocabs.toolkit.restlet.ResolveIRI#lookupIRI}.
     * Lookup data is based on the correct result
     * of {@link #testResourceMapTransformProvider2()}.
     */
    @Test
    @RunAsClient
    public final void testResolveIRILookupIRI2() {
        logger.info("In testResolveIRILookupIRI2()");
        ArquillianTestUtils.clientClearDatabase(baseURL);
        ArquillianTestUtils.clientLoadDbUnitTestFile(baseURL,
                "testResolveIRILookupIRI2");

        String service = "resolve/lookupIRI";

        String redirectPrefix =
                "http://testing-host.org.au/repository/api/lda/"
                + "ands/testresourcemaptransformprovider2/v1/resource";

        // Tests: valid IRI specified.
        String[] resourcesThatShouldResolve = {
                "http://vocab.owner.org/def/vocab1/1",
                "http://vocab.owner.org/def/vocab1/2",
                "http://vocab.owner.org/def/vocab1/3",
                "http://vocab.owner.org/def/vocab1/5",
                "http://vocab.owner.org/def/vocab1/6",
                "http://vocab.owner.org/def/vocab1/7",
                "http://vocab.owner.org/def/vocab1/9",
                "https://second.owned.com/def/vocab2/1",
                "https://second.owned.com/def/vocab2/2",
                "https://second.owned.com/def/vocab2/3",
                "https://second.owned.com/def/vocab2/5",
                "https://second.owned.com/def/vocab2/6",
                "https://second.owned.com/def/vocab2/7",
                "https://second.owned.com/def/vocab2/9"
        };

        testValidIRISpecified(service, redirectPrefix,
                resourcesThatShouldResolve);

        // Tests: invalid IRI specified: defined, but not owned
        String[] resourcesThatShouldNotResolve = {
                "http://some.other.org/def/vocab1/1",
                "http://some.other.org/def/vocab1/2",
                "http://some.other.org/def/vocab1/3",
                "http://some.other.org/def/vocab1/5",
                "http://some.other.org/def/vocab1/6",
                "http://some.other.org/def/vocab1/7",
                "http://some.other.org/def/vocab1/9"
        };

        // Tests: non-owned IRI specified.
        testInvalidIRISpecified(service, resourcesThatShouldNotResolve,
                ResolveIRI.NO_DEFINITION,
                "Error message when non-owned IRI specified");
    }

    /** Client-side test 3 of the global IRI resolver function,
     * {@link au.org.ands.vocabs.toolkit.restlet.ResolveIRI#lookupIRI}.
     * Lookup data is based on the correct result
     * of {@link #testResourceMapTransformProvider3()}.
     */
    @Test
    @RunAsClient
    public final void testResolveIRILookupIRI3() {
        logger.info("In testResolveIRILookupIRI3()");
        ArquillianTestUtils.clientClearDatabase(baseURL);
        ArquillianTestUtils.clientLoadDbUnitTestFile(baseURL,
                "testResolveIRILookupIRI3");

        String service = "resolve/lookupIRI";

        String redirectPrefix =
                "http://testing-host.org.au/repository/api/lda/"
                + "ands/testresourcemaptransformprovider3-1/v1/resource";

        // Tests: valid IRI specified.
        String[] resourcesThatShouldResolve = {
                "http://vocab.owner.org/def/vocab1/1",
                "http://vocab.owner.org/def/vocab1/2",
                "http://vocab.owner.org/def/vocab1/3",
                "http://vocab.owner.org/def/vocab1/5",
                "http://vocab.owner.org/def/vocab1/6",
                "http://vocab.owner.org/def/vocab1/7",
                "http://vocab.owner.org/def/vocab1/9",
                // vocab1/10 is multiply defined, but it should still
                // resolve, because the multiple definitions are
                // within the _same_ access point.
                "http://vocab.owner.org/def/vocab1/10",
                "https://second.owned.com/def/vocab2/1",
                "https://second.owned.com/def/vocab2/2",
                "https://second.owned.com/def/vocab2/3",
                "https://second.owned.com/def/vocab2/5",
                "https://second.owned.com/def/vocab2/6",
                "https://second.owned.com/def/vocab2/7",
                "https://second.owned.com/def/vocab2/9"
        };

        testValidIRISpecified(service, redirectPrefix,
                resourcesThatShouldResolve);

        // Tests: invalid IRI specified: no owned definition.
        String[] resourcesThatShouldNotResolveNoOwnedDefinition = {
                "http://some.other.org/def/vocab1/1",
                "http://some.other.org/def/vocab1/2",
                "http://some.other.org/def/vocab1/3",
                "http://some.other.org/def/vocab1/5",
                "http://some.other.org/def/vocab1/6",
                "http://some.other.org/def/vocab1/7",
                "http://some.other.org/def/vocab1/9"
        };

        testInvalidIRISpecified(service,
                resourcesThatShouldNotResolveNoOwnedDefinition,
                ResolveIRI.NO_DEFINITION,
                "Error message when non-owned IRI specified");

        // Tests: invalid IRI specified: definitions in multiple access points.
        String[] resourcesThatShouldNotResolveMultipleDefinitions = {
                "http://third.another.edu/def/vocab3/1",
                "http://third.another.edu/def/vocab3/2",
                "http://third.another.edu/def/vocab3/3",
                "http://third.another.edu/def/vocab3/5",
                "http://third.another.edu/def/vocab3/6",
                "http://third.another.edu/def/vocab3/7",
                "http://third.another.edu/def/vocab3/9"
        };

        testInvalidIRISpecified(service,
                resourcesThatShouldNotResolveMultipleDefinitions,
                ResolveIRI.MULTIPLE_DEFINITIONS,
                "Error message when multiply-defined IRI specified");

        // Now make one of the versions superseded, and observe how this
        // affects resolution.
        ArquillianTestUtils.clientLoadDbUnitTestFileAsUpdate(baseURL,
                "testResolveIRILookupIRI3", "input-dbunit2.xml");

        // Tests: valid IRI specified.
        testValidIRISpecified(service, redirectPrefix,
                resourcesThatShouldResolve);

        testInvalidIRISpecified(service,
                resourcesThatShouldNotResolveNoOwnedDefinition,
                ResolveIRI.NO_DEFINITION,
                "Error message when non-owned IRI specified");

        // No longer multiple definitions.
        testValidIRISpecified(service, redirectPrefix,
                resourcesThatShouldNotResolveMultipleDefinitions);

    }

    /** Test that an array of IRIs resolve to the respective desired locations.
     * Test with and without a suffix parameter provided.
     * @param service The URL component to the lookup service, to be
     *      appended to baseURL.
     * @param redirectPrefix The beginning of the desired redirect location.
     * @param resourcesThatShouldResolve An array of IRIs that are to
     *      be looked up, to confirm that they resolve to the correct
     *      location.
     */
    private void testValidIRISpecified(final String service,
            final String redirectPrefix,
            final String[] resourcesThatShouldResolve) {
        // Include an ampersand in the test suffix, as it is important
        // to confirm that this comes back without any form of escaping applied.
        String dummyTestSuffix = "&dummyTestSuffix";
        Response response;
        for (String resource : resourcesThatShouldResolve) {
            response = NetClientUtils.doGetWithAdditionalComponentsNoRedirects(
                    baseURL, service,
                    webTarget -> webTarget.queryParam("iri", resource));
            Assert.assertEquals(response.getStatusInfo().getFamily(),
                    Family.REDIRECTION,
                    "lookupIRI response status for iri: " + resource);
            URI redirectLocation = response.getLocation();
            response.close();
            Assert.assertEquals(redirectLocation.toString(),
                    redirectPrefix + "?uri=" + resource,
                    "Redirect URL for " + resource);
            // Now again, with a suffix provided.
            response = NetClientUtils.doGetWithAdditionalComponentsNoRedirects(
                    baseURL, service,
                    webTarget -> webTarget.queryParam("iri", resource)
                        .queryParam("suffix", dummyTestSuffix));
            Assert.assertEquals(response.getStatusInfo().getFamily(),
                    Family.REDIRECTION,
                    "lookupIRI response status for iri: " + resource);
            redirectLocation = response.getLocation();
            response.close();
            Assert.assertEquals(redirectLocation.toString(),
                    redirectPrefix + "?uri=" + resource + dummyTestSuffix,
                    "Redirect URL for " + resource
                    + " with suffix " + dummyTestSuffix);
        }
    }

    /** Test that an array of IRIs do not return a resolved location.
     * @param service The URL component to the lookup service, to be
     *      appended to baseURL.
     * @param resourcesThatShouldNotResolve An array of IRIs that are to
     *      be looked up, to confirm that they do not resolve.
     * @param expectedErrorMessagePrefix The beginning of the expected
     *      error message that comes back from the resolution service.
     * @param errorMessageOnFailure The assertion error to be printed,
     *      if the assertion fails.
     */
    private void testInvalidIRISpecified(final String service,
            final String[] resourcesThatShouldNotResolve,
            final String expectedErrorMessagePrefix,
            final String errorMessageOnFailure) {
        Response response;
        String body;
        for (String resource : resourcesThatShouldNotResolve) {
            response = NetClientUtils.doGetWithAdditionalComponentsNoRedirects(
                    baseURL, service,
                    webTarget -> webTarget.queryParam("iri", resource));
            Assert.assertEquals(response.getStatusInfo().getFamily(),
                    Family.CLIENT_ERROR, "lookupIRI response status, iri: "
                    + resource);
            body = response.readEntity(String.class);
            response.close();
            Assert.assertTrue(body.startsWith(expectedErrorMessagePrefix),
                    errorMessageOnFailure);
        }
    }


}
