package au.org.ands.vocabs.toolkit.provider.transform;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Properties;

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
 * Transform provider for running a SPARQL update on a Sesame repository. In
 * case we need to do a transform provider that operates on "raw" RDF files, see
 * https://groups.google.com/d/msg/sesame-users/fJctKX_vNEs/a1gm7rqD3L0J for how
 * to do it.
 */
public class SesameSPARQLUpdateTransformProvider extends TransformProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL to access the Sesame server. */
    private String sesameServer =
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

    @Override
    public final String getInfo() {
        // Not implemented.
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        RepositoryManager manager = null;
        if (subtask.get("sparql_update") == null) {
            TasksUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "No SPARQL update statement specified.");
            return false;
        }
        String sparqlUpdateText = subtask.get("sparql_update").asText();
                // First, open the repository
        Repository repository;
        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);

            String repositoryID = ToolkitFileUtils.getTaskRepositoryId(
                    taskInfo);

            repository = manager.getRepository(repositoryID);
            if (repository == null) {
                logger.error("SesameSPARQLUpdate: no such repository: "
                        + repositoryID);
                TasksUtils.updateMessageAndTaskStatus(logger,
                        taskInfo.getTask(),
                        results, TaskStatus.ERROR,
                        "SesameSPARQLUpdate: no such repository: "
                                + repositoryID);
                return false;
            }
        } catch (RepositoryConfigException | RepositoryException e) {
            logger.error("Exception in SesameSPARQLUpdate opening "
                    + "repository", e);
            TasksUtils.updateMessageAndTaskStatus(logger,
                    taskInfo.getTask(),
                    results, TaskStatus.EXCEPTION,
                    "Exception in SesameSPARQLUPdate opening repository");
            return false;
        }
        // Now, open a connection and process the update
        try {
            RepositoryConnection conn = null;
            try {
                conn = repository.getConnection();
                Update update = conn.prepareUpdate(QueryLanguage.SPARQL,
                        sparqlUpdateText);
                update.execute();
            } catch (MalformedQueryException e) {
                logger.error("Bad update passed to SesameSPARQLUpdate: "
                        + sparqlUpdateText, e);
                TasksUtils.updateMessageAndTaskStatus(logger,
                        taskInfo.getTask(),
                        results, TaskStatus.EXCEPTION,
                        "Bad update passed to SesameSPARQLUpdate: "
                                + sparqlUpdateText);
                return false;
            } catch (UpdateExecutionException e) {
                logger.error("SesameSPARQLUpdate update failed: "
                        + sparqlUpdateText, e);
                TasksUtils.updateMessageAndTaskStatus(logger,
                        taskInfo.getTask(),
                        results, TaskStatus.EXCEPTION,
                        "SesameSPARQLUpdate update failed: "
                                + sparqlUpdateText);
                return false;
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (RepositoryException e) {
            logger.error("Exception in SesameSPARQLUpdate with "
                    + "connection handling", e);
            TasksUtils.updateMessageAndTaskStatus(logger,
                    taskInfo.getTask(),
                    results, TaskStatus.EXCEPTION,
                    "Exception in SesameSPARQLUPdate with "
                            + "connection handling");
            return false;
        }
        return true;
    }

}
