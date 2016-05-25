/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.io.FileUtils;
import org.dbunit.DatabaseUnitException;
import org.hibernate.HibernateException;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskRunner;
import au.org.ands.vocabs.toolkit.test.utils.NetClientUtils;
import au.org.ands.vocabs.toolkit.utils.ApplicationContextListener;
import au.org.ands.vocabs.toolkit.utils.ToolkitConfig;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

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

    // Test setup/shutdown

    /** Set up the suite. This means:
     * clear out the contents of the repository (deleting
     * the directory pointed to by property {@code Toolkit.storagePath}).
     * Note: Arquillian invokes this method first on the client side, and
     * then on the server side after deployment.
     * @throws IOException If unable to remove the repository directory
     *      {@code Toolkit.storagePath}.
     */
    @BeforeSuite(groups = "arquillian")
    public final void setupSuite() throws IOException {
        if (ApplicationContextListener.getServletContext() == null) {
            logger.info("In AllArquillianTests.setupSuite() on client side");
        } else {
            logger.info("In AllArquillianTests.setupSuite() on server side");
            FileUtils.deleteDirectory(new File(
                    ToolkitConfig.ROOT_FILES_PATH));
        }
    }

    // Server-side tests go here. Client-side tests later on.

    // Tests of class au.org.ands.vocabs.toolkit.db.TasksUtils.

    /** Server-side test of {@code TasksUtils.getAllTasks()}
     * when there are no tasks. */
    @Test
    public final void testGetAllTasks() {
        logger.info("In testGetAllTasks()");
        List<Task> taskList = TasksUtils.getAllTasks();
        Assert.assertNotNull(taskList,
                "getAllTasks() with no tasks");
        Assert.assertEquals(taskList.size(), 0,
                "getAllTasks() with no tasks");
    }

    // Tests of class
    // au.org.ands.vocabs.toolkit.provider.transform.JsonTreeTransformProvider.

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
        ArquillianTestUtils.loadDbUnitTestFile(
                "testJsonTreeTransformProvider1");
        List<Task> taskList = TasksUtils.getAllTasks();
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
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws IOException If a problem getting test data for DBUnit.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    @Test
    @RunAsClient
    public final void testSystemHealthCheck() throws
        DatabaseUnitException, IOException, SQLException {
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

}
