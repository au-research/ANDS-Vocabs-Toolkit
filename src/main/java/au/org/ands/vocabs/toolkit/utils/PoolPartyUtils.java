/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.utils;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import au.org.ands.vocabs.editor.admin.model.PoolPartyProject;

/** Utility methods for working with PoolParty. */
public final class PoolPartyUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private PoolPartyUtils() {
    }

    /** Supplement to be appended to {@code
     * ToolkitProperties.getProperty(PropertyConstants.POOLPARTY_REMOTEURL)}
     * to give the base of project-related API calls.
     * Then after this, append the project's UUID, and then
     * the specific API call.
     */
    public static final String API_PROJECTS = "api/projects";

    /** Supplement to be appended to {@code ToolkitProperties.getProperty(
     * PropertyConstants.POOLPARTY_REMOTEURL)} to give the base
     * of SPARQL endpoints. Then after this, append the project's
     * {@code uriSupplement}.
     */
    private static final String SPARQL = "sparql";

    /** Get the top-level metadata of all PoolParty projects.
     * @return The user's PoolParty projects as an array of instances of
     *         PoolPartyProject. */
    public static PoolPartyProject[] getPoolPartyProjects() {
        String remoteUrl = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_REMOTEURL);
        String username = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_USERNAME);
        String password = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_PASSWORD);

        Client client = ClientBuilder.newClient();
        // Need to register the Jackson provider in order
        // to deserialize the JSON returned by PoolParty.
        client.register(JacksonJaxbJsonProvider.class);

        WebTarget target = client.target(remoteUrl)
                .path(API_PROJECTS);
        LOGGER.debug("Getting PoolParty metadata from " + target.getUri());
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(username, password);
        target.register(feature);

        Invocation.Builder invocationBuilder =
                target.request(MediaType.APPLICATION_JSON);

        Response response = invocationBuilder.get();

        LOGGER.debug("getPoolPartyProjects response code: "
        + response.getStatus());
        if (response.getStatus() >= Status.BAD_REQUEST.getStatusCode()) {
            // Login failed.
            return null;
        }

        PoolPartyProject[] projectsArray =
                response.readEntity(new
                        GenericType<PoolPartyProject[]>() { });
        LOGGER.debug("projectsList length = " + projectsArray.length);
        return projectsArray;
    }

    /** Regular expression defining the placeholder to use in queries
     * and updates, to be replaced with
     * the IRI of the named graph that contains the project's
     * thesaurus data. The replacement text will include angle brackets;
     * therefore, do not include them in the template. Sample uses within
     * a query:
     * <pre>
     * SELECT ?s FROM #THESAURUS# WHERE { ?s ... }
     * SELECT ?s FROM #THESAURUS/deprecated# WHERE { ?s ... }
     * </pre>
     * */
    private static final String PROJECT_THESAURUS_DATA_GRAPH =
            "#THESAURUS(/[^#]+)?#";

    /** Regular expression defining the placeholder to use in queries
     * and updates, to be replaced with
     * the IRI of the named graph that contains the project's
     * thesaurus data. The replacement text will include angle brackets;
     * therefore, do not include them in the template. Sample uses within
     * a query:
     * <pre>
     * SELECT ?s FROM #THESAURUS# WHERE { ?s ... }
     * SELECT ?s FROM #THESAURUS/deprecated# WHERE { ?s ... }
     * </pre>
     * */
    private static final String PROJECT_METADATA_GRAPH =
            "#METADATA(/[^#]+)?#";

    /** Get the IRI of the named graph containing the project's metadata,
     * with a substitution element $1 for a suffix.
     * The result has surrounding angle brackets.
     * The result of this method is intended to be used as the second parameter
     * to the method {@link String#replaceAll(String, String)}, where
     * the value of the second parameter contains one capturing group.
     * @param project The PoolParty project definition.
     * @return The IRI of the named graph, as a String containing $1.
     *   Example: <code>&lt;http://path.to.api/1234/metadata$1&gt;</code>
     */
    private static String getMetadataGraph(final PoolPartyProject project) {
        return "<" + project.getUri() + "/metadata" + "$1" + ">";
    }

    /** Get the IRI of the named graph containing the project's thesaurus data,
     * with a substitution element $1 for a suffix.
     * The result has surrounding angle brackets.
     * The result of this method is intended to be used as the second parameter
     * to the method {@link String#replaceAll(String, String)}, where
     * the value of the second parameter contains one capturing group.
     * @param project The PoolParty project definition.
     * @return The IRI of the named graph, as a String containing $1.
     *   Example: <code>&lt;http://path.to.api/1234/thesaurus$1&gt;</code>
     */
    private static String getThesaurusGraph(final PoolPartyProject project) {
        return "<" + project.getUri() + "/thesaurus" + "$1" + ">";
    }

    /** Run a SPARQL query against a project.
     * @param poolPartyProject The PoolParty project.
     * @param query The template of the SPARQL query to run. It may use
     *      template parameters {@code #THESAURUS...#} and
     *      {@code #METADATA...#}; uses of these parameters are expanded
     *      before the query is sent.
     * @param format The value of the format to send in the query. For example,
     *      use {@link MediaType#APPLICATION_XML} for a {@code SELECT} query,
     *      and use, e.g., {@code "text/turtle"} for a {@code CONSTRUCT}
     *      query.
     * @return The results of running the query. */
    public static String runQuery(
            final PoolPartyProject poolPartyProject,
            final String query,
            final String format) {
        String remoteUrl = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_REMOTEURL);
        String username = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_USERNAME);
        String password = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_PASSWORD);

        Client client = ClientBuilder.newClient();

        String uriSupplement = poolPartyProject.getUriSupplement();
        WebTarget target = client.target(remoteUrl)
                .path(SPARQL).path(uriSupplement);
        LOGGER.debug("Running query: " + target.getUri());
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(username, password);
        target.register(feature);

        Form queryForm = new Form();
        String projectThesaurusGraph = getThesaurusGraph(poolPartyProject);
        String projectMetadataGraph = getMetadataGraph(poolPartyProject);

        String sparql = query.replaceAll(PROJECT_THESAURUS_DATA_GRAPH,
                projectThesaurusGraph).
                replaceAll(PROJECT_METADATA_GRAPH,
                        projectMetadataGraph);

        queryForm.param("query", sparql);
        queryForm.param("format", format);

        Invocation.Builder invocationBuilder =
                target.request();

        Response response = invocationBuilder.post(Entity.entity(queryForm,
                MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        LOGGER.debug("runQuery response code: " + response.getStatus());
        if (response.getStatus() >= Status.BAD_REQUEST.getStatusCode()) {
            // Query failed.
            return null;
        }

        String responseSparql =
                response.readEntity(String.class);

        return responseSparql;
    }

}
