/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.harvest;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.editor.admin.model.PoolPartyProject;
import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.provider.transform.GetMetadataTransformProvider;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.PoolPartyUtils;
import au.org.ands.vocabs.toolkit.utils.PropertyConstants;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitNetUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Harvest provider for PoolParty. */
public class PoolPartyHarvestProvider extends HarvestProvider {

    /** The logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    @Override
    public final String getInfo() {
        String remoteUrl = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_REMOTEURL);
        String username = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_USERNAME);
        String password = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_PASSWORD);

        logger.debug("Getting metadata from " + remoteUrl);

        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(remoteUrl).
                path(PoolPartyUtils.API_PROJECTS);
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(username, password);
        target.register(feature);

        Invocation.Builder invocationBuilder =
                target.request(MediaType.APPLICATION_JSON);

        Response response = invocationBuilder.get();

        // This is how you do it if you want to parse the JSON.
        //            InputStream is = response.readEntity(InputStream.class);
        //            JsonReader jsonReader = Json.createReader(is);
        //            JsonStructure jsonStructure = jsonReader.readArray();
        //            return jsonStructure.toString();

        // Oops, for tidyness, should close the Response object here.
        // But the Jersey implementation closes the underlying
        // resources as part of readEntity().
        return response.readEntity(String.class);
    }

    /** Template for a SPARQL CONSTRUCT Query to get the full names of users
     * mentioned in a PoolParty project. Each such piece of information
     * is returned as two triples, of the form
     * {@code ?agent a dcterms:Agent; foaf:name ?fullname}.
     * Why use FOAF? (a) To follow the examples given at
     * <a href=
     * "http://wiki.dublincore.org/index.php/User_Guide/Publishing_Metadata">
     * http://wiki.dublincore.org/index.php/User_Guide/Publishing_Metadata</a>.
     * (b) In fact, the ADMS graph also uses {@code foaf:name}
     * for this purpose.
     */
    private static final String GET_USER_FULLNAMES_TEMPLATE =
        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
      + "PREFIX dcterms: <http://purl.org/dc/terms/>\n"
      + "CONSTRUCT { ?agent a dcterms:Agent; foaf:name ?fullname }\n"
      + "FROM #THESAURUS#\n"
      + "FROM #METADATA/void#\n"
      + "FROM NAMED #THESAURUS/users#\n"
      + "WHERE {\n"
      + "  {\n"
      + "    ?s ?p ?agent\n"
      + "    FILTER (STRSTARTS(STR(?p), \"http://purl.org/dc/terms/\"))\n"
      + "  }\n"
      + "  GRAPH #THESAURUS/users# {\n"
      + "    ?agent <http://schema.semantic-web.at/users/username> ?fullname\n"
      + "  }\n"
      + "}\n";

    /** Do a harvest. Update the message parameter with the result
     * of the harvest.
     * @param ppProjectId The PoolParty project id.
     * @param outputPath The directory in which to store output files.
     * @param getMetadata Whether or not to get ADMS and VOID metadata
     * @param returnOutputPaths Whether or not to store the full path
     * of each harvested file in the results map.
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    public final boolean getHarvestFiles(final String ppProjectId,
            final String outputPath,
            final boolean getMetadata,
            final boolean returnOutputPaths,
            final HashMap<String, String> results) {
        String remoteUrl = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_REMOTEURL);
        String username = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_USERNAME);
        String password = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_PASSWORD);

        String format = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTYHARVESTER_DEFAULTFORMAT);

// Possible future work: support specifying particular modules.
//        List<String> exportModules =
//                info.getQueryParameters().get("exportModules");
        List<String> exportModules = new ArrayList<String>();
        exportModules.add(ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTYHARVESTER_DEFAULTEXPORTMODULE));
        // Separation of deprecated concepts happened in PoolParty 5.5.
        exportModules.add("deprecatedConcepts");
        if (getMetadata) {
            exportModules.add("adms");
            exportModules.add("void");
        }

        logger.debug("Getting project from " + remoteUrl);

        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(remoteUrl).
                path(PoolPartyUtils.API_PROJECTS);
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(username, password);
        WebTarget plainTarget = target.register(feature)
                .path(ppProjectId)
                .path("export")
                .queryParam("format", format);

        for (String exportModule : exportModules) {
            results.put("poolparty_url", remoteUrl);
            results.put("poolparty_project_id", ppProjectId);
            WebTarget thisTarget = plainTarget.queryParam("exportModules",
                    exportModule);

            logger.debug("Harvesting from " + thisTarget.toString());

            Invocation.Builder invocationBuilder =
                    thisTarget.request(MediaType.APPLICATION_XML);

            Response response = invocationBuilder.get();

            if (response.getStatus()
                    >= Response.Status.BAD_REQUEST.getStatusCode()) {
                logger.error("getHarvestFiles got an error from PoolParty; "
                        + "response code = " + response.getStatus());

                results.put(TaskStatus.ERROR,
                        "PoolPartyHarvestProvider.getHarvestFiles() "
                        + "got an error from PoolParty; "
                        + "response code = " + response.getStatus());
                return false;
            }

            String responseData = response.readEntity(String.class);

            String filePath = ToolkitFileUtils.saveFile(
                    outputPath,
                    exportModule,
                    format, responseData);
            if (returnOutputPaths) {
                results.put(exportModule, filePath);
            }

            // Clean up Response object.
            response.close();
        }

        // In order to get content contained in the users named graph,
        // we have to do a SPARQL query. But to do that, we need
        // to use an API call, for which the URL contains the
        // project's "uriSupplement". But to get that, we have
        // to use the API call to get the top-level metadata of
        // _all_ projects! And then we do a search in the result
        // to find this particular project.
        PoolPartyProject[] poolPartyProjects =
                PoolPartyUtils.getPoolPartyProjects();
        // We get back an unsorted array, so a linear search is called for.
        int projectsLength = poolPartyProjects.length;
        int projectIndex = 0;
        boolean found = false;
        while (projectIndex < projectsLength) {
            if (ppProjectId.equals(poolPartyProjects[projectIndex].getId())) {
                // Found it.
                found = true;
                break;
            }
            projectIndex++;
        }
        if (!found) {
            logger.error("getHarvestFiles was unable to get project metadata, "
                    + "even after an export! "
                    + "This should not happen!");
            return false;
        }
        String usersGraphContents = PoolPartyUtils.runQuery(
                poolPartyProjects[projectIndex],
                GET_USER_FULLNAMES_TEMPLATE,
                GetMetadataTransformProvider.USERS_GRAPH_FORMAT.
                    getDefaultMIMEType());
        File usersGraphFile = new File(
                Paths.get(outputPath).
                resolve(GetMetadataTransformProvider.USERS_GRAPH_FILE).
                toString());
        try {
            FileUtils.writeStringToFile(usersGraphFile, usersGraphContents,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            results.put(TaskStatus.EXCEPTION,
                    "PoolPartyHarvester: can't write user details file.");
            logger.error("PoolPartyHarvester getHarvestFiles: "
                    + "can't write user details file.",
                    e);
            return false;
        }

        return true;
    }

    /** Do a harvest. Update the result parameter with the result
     * of the harvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    @Override
    public final boolean harvest(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        if (subtask.get("project_id") == null
                || subtask.get("project_id").textValue().isEmpty()) {
            TaskUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "No PoolParty id specified. Nothing to do.");
            return false;
        }

        String projectId = subtask.get("project_id").textValue();
        return getHarvestFiles(projectId,
                ToolkitFileUtils.getTaskHarvestOutputPath(taskInfo),
                false, true, results);
    }

    /** Get metadata for a PoolParty project.
     * @param projectId The PoolParty Project Id.
     * @return The metadata for the project.
     */
    @Override
    public final HashMap<String, String> getMetadata(final String projectId) {

        HashMap<String, String> result =
                new HashMap<String, String>();

        getHarvestFiles(projectId,
                ToolkitFileUtils.getMetadataOutputPath(projectId),
                true, false, result);
        return result;
    }

}
