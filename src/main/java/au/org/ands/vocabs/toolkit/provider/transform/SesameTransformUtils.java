/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility methods for working on a Sesame repository.
 */
public final class SesameTransformUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL to access the Sesame server. */
    private static String sesameServer =
            PROPS.getProperty("SesameImporter.serverUrl");

    /** Force loading of HttpClientUtils, so that shutdown works
     * properly. Revisit this when using a later version of Tomcat,
     * as the problem may be caused by a defect in Tomcat.
     * For now (Tomcat 7.0.61), without this, you get an error
     * on Tomcat shutdown:
     * <pre>
     * Exception in thread "RepositoryProvider-shutdownHook"
     *  java.lang.NoClassDefFoundError:
     *   org/apache/http/client/utils/HttpClientUtils
     *    at org.openrdf.http.client.SesameClientImpl.shutDown(
     *    SesameClientImpl.java:102)
     * at org.openrdf.repository.manager.RemoteRepositoryManager.shutDown(
     *    RemoteRepositoryManager.java:156)
     * at org.openrdf.repository.manager.
     *   RepositoryProvider$SynchronizedManager.shutDown(
     *     RepositoryProvider.java:68)
     * at org.openrdf.repository.manager.RepositoryProvider$1.run(
     *   RepositoryProvider.java:81)
     * Caused by: java.lang.ClassNotFoundException:
     *    org.apache.http.client.utils.HttpClientUtils
     *  at org.apache.catalina.loader.WebappClassLoader.loadClass(
     *     WebappClassLoader.java:1720)
     *  at org.apache.catalina.loader.WebappClassLoader.loadClass(
     *     WebappClassLoader.java:1571)
     *  ... 4 more
     *  </pre>
     *
     */
    @SuppressWarnings("unused")
    private static final Class<org.apache.http.client.utils.HttpClientUtils>
        HTTPCLIENTUTILS_CLASS =
            org.apache.http.client.utils.HttpClientUtils.class;

    /** Private constructor for a utility class. */
    private SesameTransformUtils() {
    }

    /** Run a SPARQL Update on a repository.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The specification of this transform subtask
     * @param results HashMap representing the result of the transform.
     * @param updateString The text of the SPARQL Update to run.
     * @param bindings Any bindings that are to be applied. Keys are
     * variable names (without leading "?"); values are the corresponding
     * values to be bound.
     * @return True, iff the update succeeded.
    */
    public static boolean runUpdate(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results,
            final String updateString,
            final HashMap<String, Value> bindings) {
        RepositoryManager manager = null;
        // First, open the repository
        Repository repository;
        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);

            String repositoryID = ToolkitFileUtils.getSesameRepositoryId(
                    taskInfo);

            repository = manager.getRepository(repositoryID);
            if (repository == null) {
                LOGGER.error("SesameTransformUtils.runUpdate(): "
                        + "no such repository: "
                        + repositoryID);
                TasksUtils.updateMessageAndTaskStatus(LOGGER,
                        taskInfo.getTask(),
                        results, TaskStatus.ERROR,
                        "SesameTransformUtils.runUpdate(): no such repository: "
                                + repositoryID);
                return false;
            }
        } catch (RepositoryConfigException | RepositoryException e) {
            LOGGER.error("Exception in SesameTransformUtils.runUpdate() "
                    + "opening repository", e);
            TasksUtils.updateMessageAndTaskStatus(LOGGER,
                    taskInfo.getTask(),
                    results, TaskStatus.EXCEPTION,
                    "Exception in SesameTransformUtils.runUpdate() "
                            + "opening repository");
            return false;
        }
        // Now, open a connection and process the update
        try {
            RepositoryConnection conn = null;
            try {
                conn = repository.getConnection();
                Update update = conn.prepareUpdate(QueryLanguage.SPARQL,
                        updateString);
                for (Entry<String, Value> binding : bindings.entrySet()) {
                    update.setBinding(binding.getKey(), binding.getValue());
                }
                update.execute();
            } catch (MalformedQueryException e) {
                LOGGER.error("Bad update passed to "
                        + "SesameTransformUtils.runUpdate(): "
                        + updateString, e);
                TasksUtils.updateMessageAndTaskStatus(LOGGER,
                        taskInfo.getTask(),
                        results, TaskStatus.EXCEPTION,
                        "Bad update passed to "
                                + "SesameTransformUtils.runUpdate(): "
                                + updateString);
                return false;
            } catch (UpdateExecutionException e) {
                LOGGER.error("SesameTransformUtils.runUpdate() update failed: "
                        + updateString, e);
                TasksUtils.updateMessageAndTaskStatus(LOGGER,
                        taskInfo.getTask(),
                        results, TaskStatus.EXCEPTION,
                        "SesameTransformUtils.runUpdate() update failed: "
                                + updateString);
                return false;
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("Exception in SesameTransformUtils.runUpdate() with "
                    + "connection handling", e);
            TasksUtils.updateMessageAndTaskStatus(LOGGER,
                    taskInfo.getTask(),
                    results, TaskStatus.EXCEPTION,
                    "Exception in SesameTransformUtils.runUpdate() with "
                            + "connection handling");
            return false;
        }
        return true;
    }

}
