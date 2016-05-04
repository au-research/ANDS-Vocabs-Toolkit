/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.test.utils.NetClientUtils;

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

    // Server-side tests go here. Client-side tests later on.


    // Tests of class au.org.ands.vocabs.toolkit.db.TasksUtils.

    /** Server-side test of {@code TasksUtils.getAllTasks()}
     * when there are no tasks. */
    @Test
    public final void testGetAllTasks() {
        logger.info("In testGetAllTasks");
        List<Task> taskList = TasksUtils.getAllTasks();
        Assert.assertNotNull(taskList,
                "getAllTasks() with no tasks");
        Assert.assertEquals(taskList.size(), 0,
                "getAllTasks() with no tasks");
        // Assert.fail("Test of failing in testGetAllTasks");
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

    /** Client-side test of the system health check function. */
    @Test
    @RunAsClient
    public final void testSystemHealthCheck(
            ) {
        logger.info("In testSystemHealthCheck");
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
