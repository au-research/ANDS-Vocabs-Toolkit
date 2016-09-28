/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.db.AccessPointUtils;
import au.org.ands.vocabs.toolkit.db.ResourceMapEntryUtils;
import au.org.ands.vocabs.toolkit.db.ResourceOwnerHostUtils;
import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.db.model.AccessPoint;
import au.org.ands.vocabs.toolkit.db.model.ResourceOwnerHost;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.PropertyConstants;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/**
 * Transform provider for adding/removing resource map entries.
 *
 * Prerequisite for this transform is that the version must have exactly
 * one access point of type "sissvoc".
 */
public class ResourceMapTransformProvider extends TransformProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** URL to access the Sesame server. */
    private static String sesameServer = ToolkitProperties.getProperty(
            PropertyConstants.SESAMEIMPORTER_SERVERURL);

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

    /** The default setting for {@code fail_on_error}.
     */
    private static final boolean FAIL_ON_ERROR_DEFAULT = false;

    /** Array of resource types of interest. */
    private static URI[] resourceTypes = {
            SKOS.CONCEPT,
            SKOS.CONCEPT_SCHEME,
            SKOS.COLLECTION
    };

    /* After template replacement, the finished query sent to the repository
       looks something like this:
        SELECT ?iri {
          ?iri a ?type
          FILTER (?type IN (
            <http://www.w3.org/2004/02/skos/core#ConceptScheme>,
            <http://www.w3.org/2004/02/skos/core#Collection>,
            <http://www.w3.org/2004/02/skos/core#Concept>))
          FILTER (REGEX(STR(?iri),"^https?://(abcd\\.org|efgh\\.org)/","i"))
        }
     */

    /** Name of the binding for iri
     *  used within {@link EXTRACT_IRIS_QUERY_TEMPLATE}.
     */
    private static final String BINDING_NAME_IRI = "iri";

    /** Name of the binding for type
     *  used within {@link EXTRACT_IRIS_QUERY_TEMPLATE}.
     */
    private static final String BINDING_NAME_TYPE = "type";

    /** Name of the binding for owned
     *  used within {@link EXTRACT_IRIS_QUERY_TEMPLATE}.
     */
    private static final String BINDING_NAME_OWNED = "owned";

    /** Name of the binding for deprecated
     *  used within {@link EXTRACT_IRIS_QUERY_TEMPLATE}.
     */
    private static final String BINDING_NAME_DEPRECATED = "deprecated";

    /** Template for a SPARQL Query to extract exactly those IRIs
     * to be added to the concept map. The template elements
     * #RESOURCETYPES# and #HOSTNAMES# are replaced by
     * {@link #transform(TaskInfo, JsonNode, HashMap)}.
     * When running this query, turn off inferred statements, otherwise
     * deprecated resources that don't have a defined type will go missing.
     * (If inferring is on, they get the inferred type rdfs:Resource.)
     */
    private static final String EXTRACT_IRIS_QUERY_TEMPLATE =
            "SELECT ?iri ?type ?owned ?deprecated {"
            + "  {"
            + "    ?iri a ?type"
            + "    FILTER (?type IN (#RESOURCETYPES#))"
            + "    OPTIONAL {"
            + "      ?iri <http://www.w3.org/2002/07/owl#deprecated> true"
            + "      BIND (true AS ?found_deprecated)"
            + "    }"
            + "    BIND (BOUND(?found_deprecated) AS ?deprecated)"
            + "  } UNION {"
            + "    ?iri <http://www.w3.org/2002/07/owl#deprecated> true"
            + "    FILTER NOT EXISTS { ?iri a ?another_type }"
            + "    BIND (<http://www.w3.org/2002/07/owl#deprecated> AS ?type)"
            + "    BIND (true AS ?deprecated)"
            + "  }"
            + "  BIND (REGEX(STR(?iri),\"^https?://(#HOSTNAMES#)/\",\"i\")"
            + "    AS ?owned)"
            + "}";

    /** Determine the access point ID associated with the subtask.
     * If there is not exactly one such access point, null is returned.
     * @param taskInfo The TaskInfo for this subtask.
     * @return The access point ID associated with this subtask,
     *      or null, if there is not exactly one such access point.
     */
    private Integer getAccessPointId(final TaskInfo taskInfo) {
        List<AccessPoint> aps =
                AccessPointUtils.getAccessPointsForVersionAndType(
                        taskInfo.getVersion(), AccessPoint.SISSVOC_TYPE);
        if (aps.size() != 1) {
            return null;
        }
        return aps.get(0).getId();
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        Integer accessPointId = getAccessPointId(taskInfo);
        if (accessPointId == null) {
            logNotExactlyOneSissvocAccessPoint(taskInfo, results);
            // Return failure (i.e., false) in this case,
            // if fail_on_error is set to true.
            return !TaskUtils.isSubtaskFailOnError(subtask,
                    FAIL_ON_ERROR_DEFAULT);
        }

        String owner = taskInfo.getVocabulary().getOwner();
        List<ResourceOwnerHost> resourceOwnerHosts =
                ResourceOwnerHostUtils.getResourceOwnerHostMapEntriesForOwner(
                        owner);

        if (resourceOwnerHosts.size() == 0) {
            // This owner has no hosts associated with it. So there
            // is nothing more to be done.
            logNoHosts(taskInfo, results);
            // Return failure (i.e., false) in this case,
            // if fail_on_error is set to true.
            return !TaskUtils.isSubtaskFailOnError(subtask,
                    FAIL_ON_ERROR_DEFAULT);
        }

        // Join the resource owner hostnames together to get a String
        // "host\\.name\\.one|host\\.name\\.two".
        String resourceOwnerRegex =
                resourceOwnerHosts.stream()
                .map(roh -> roh.getHost().replaceAll("\\.", "\\\\\\\\."))
                .collect(Collectors.joining("|"));
        // Join the resource types together to get a String
        // "<http://...>, <http://...>, <http://...>"
        // for insertion into the SPARQL query.
        String resourceTypesString =
                Arrays.asList(resourceTypes).stream()
                .map(i -> "<" + i.toString() + ">")
                .collect(Collectors.joining(", "));
        // Now do search/replace of placeholders in the query string.
        String queryString =
                EXTRACT_IRIS_QUERY_TEMPLATE.replace(
                        "#RESOURCETYPES#", resourceTypesString)
                .replace("#HOSTNAMES#", resourceOwnerRegex);

        RepositoryManager manager = null;
        Repository repository;
        // First, open the repository.
        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);
            String repositoryID = ToolkitFileUtils.getSesameRepositoryId(
                    taskInfo);
            repository = manager.getRepository(repositoryID);
            if (repository == null) {
                logger.error("ResourceMapTransformProvider.transform(): "
                        + "no such repository: "
                        + repositoryID);
                TaskUtils.updateMessageAndTaskStatus(logger,
                        taskInfo.getTask(),
                        results, TaskStatus.ERROR,
                        "ResourceMapTransformProvider.transform(): "
                        + "no such repository: " + repositoryID);
                // Return failure (i.e., false) if fail_on_error is set to true.
                return !TaskUtils.isSubtaskFailOnError(subtask,
                        FAIL_ON_ERROR_DEFAULT);
            }
        } catch (RepositoryConfigException | RepositoryException e) {
            logger.error("Exception in ResourceMapTransformProvider."
                    + "transform() opening repository", e);
            TaskUtils.updateMessageAndTaskStatus(logger,
                    taskInfo.getTask(),
                    results, TaskStatus.EXCEPTION,
                    "Exception in ResourceMapTransformProvider.transform() "
                            + "opening repository");
            // An exception: always return false in this case.
            return false;
        }

        // Clear out any existing entries before proceeding.
        ResourceMapEntryUtils.deleteResourceMapEntriesForAccessPoint(
                accessPointId);
        // Now, open a connection and process the resources.
        try {
            RepositoryConnection conn = null;
            TupleQueryResult queryResult = null;
            try {
                conn = repository.getConnection();
                try {
                    TupleQuery query =
                            conn.prepareTupleQuery(QueryLanguage.SPARQL,
                            queryString);
                    // Don't include inferred results. Doing so causes
                    // deprecated concepts without a defined type to
                    // go missing from query results.
                    query.setIncludeInferred(false);
                    queryResult = query.evaluate();
                    while (queryResult.hasNext()) {
                        BindingSet aBinding = queryResult.next();
                        Value iri = aBinding.getBinding(BINDING_NAME_IRI)
                                .getValue();
                        LiteralImpl owned = (LiteralImpl)
                                (aBinding.getBinding(BINDING_NAME_OWNED)
                                        .getValue());
                        Value resourceType = aBinding.
                                getBinding(BINDING_NAME_TYPE).getValue();
                        LiteralImpl deprecated = (LiteralImpl)
                                (aBinding.getBinding(BINDING_NAME_DEPRECATED)
                                        .getValue());
                        ResourceMapEntryUtils.addResourceMapEntry(
                                iri.stringValue(), accessPointId,
                                owned.booleanValue(),
                                resourceType.stringValue(),
                                deprecated.booleanValue());
                    }
                    queryResult.close();
                } catch (MalformedQueryException | QueryEvaluationException e) {
                    logger.error("Bad query constructed in "
                            + "ResourceMapTransformProvider.transform(): "
                            + queryString, e);
                    TaskUtils.updateMessageAndTaskStatus(logger,
                            taskInfo.getTask(),
                            results, TaskStatus.EXCEPTION,
                            "Bad query constructed in "
                            + "ResourceMapTransformProvider.transform(): "
                            + queryString);
                    // An exception: always return false in this case.
                    return false;
                }
            } finally {
                if (queryResult != null) {
                    queryResult.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (RepositoryException | QueryEvaluationException e) {
            logger.error("Exception in ResourceMapTransformProvider."
                    + "transform() with connection handling", e);
            TaskUtils.updateMessageAndTaskStatus(logger,
                    taskInfo.getTask(),
                    results, TaskStatus.EXCEPTION,
                    "Exception in ResourceMapTransformProvider."
                    + "transform() with connection handling");
            // An exception: always return false in this case.
            return false;
        }

        // Subtask completed successfully.
        return true;
    }

    /** Log the fact that there is not exactly one access point of
     * type "sissvoc".
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the transform.
     */
    private void logNotExactlyOneSissvocAccessPoint(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        logger.error("ResourceMapTransformProvider.transform(): "
                + "not exactly one sissvoc access point for version: "
                + taskInfo.getVersion());
        TaskUtils.updateMessageAndTaskStatus(logger,
                taskInfo.getTask(),
                results, TaskStatus.ERROR,
                "ResourceMapTransformProvider.transform(): "
                + "not exactly one sissvoc access point for version: "
                + taskInfo.getVersion());
    }

    /** Log the fact that there are no hosts associated with the
     * owner of the vocabulary.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the transform.
     */
    private void logNoHosts(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        logger.debug("ResourceMapTransformProvider.transform(): "
                + "no hosts associated with this owner: "
                + taskInfo.getVersion());
        TaskUtils.updateMessageAndTaskStatus(logger,
                taskInfo.getTask(),
                results, TaskStatus.SUCCESS,
                "ResourceMapTransformProvider.transform(): "
                + "no hosts associated with this owner: "
                + taskInfo.getVersion());
    }

    @Override
    public final boolean untransform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        Integer accessPointId = getAccessPointId(taskInfo);
        if (accessPointId == null) {
            logger.error("ResourceMapTransformProvider.untransform(): "
                    + "not exactly one sissvoc access point for version: "
                    + taskInfo.getVersion());
            TaskUtils.updateMessageAndTaskStatus(logger,
                    taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "ResourceMapTransformProvider.untransform(): "
                    + "not exactly one sissvoc access point for version: "
                    + taskInfo.getVersion());
            // Return failure (i.e., false) in this case,
            // if fail_on_error is set to true.
            return !TaskUtils.isSubtaskFailOnError(subtask,
                    FAIL_ON_ERROR_DEFAULT);
        }

        ResourceMapEntryUtils.deleteResourceMapEntriesForAccessPoint(
                accessPointId);

        // Subtask completed successfully.
        return true;
    }

}
