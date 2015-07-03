/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.harvest;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

import com.fasterxml.jackson.databind.JsonNode;

/** Harvest provider for PoolParty. */
public class PoolPartyHarvestProvider extends HarvestProvider {

    /** The logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    @Override
    public final String getInfo() {
        String remoteUrl = PROPS.getProperty("PoolPartyHarvester.remoteUrl");
        String username = PROPS.getProperty("PoolPartyHarvester.username");
        String password = PROPS.getProperty("PoolPartyHarvester.password");

        logger.debug("Getting metadata from " + remoteUrl);

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(remoteUrl);
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
        return response.readEntity(String.class);
    }

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
        String remoteUrl = PROPS.getProperty("PoolPartyHarvester.remoteUrl");
        String username = PROPS.getProperty("PoolPartyHarvester.username");
        String password = PROPS.getProperty("PoolPartyHarvester.password");

        String format = PROPS.getProperty("PoolPartyHarvester.defaultFormat");

// Possible future work: support specifying particular modules.
//        List<String> exportModules =
//                info.getQueryParameters().get("exportModules");
        List<String> exportModules = new ArrayList<String>();
        exportModules.add(PROPS.getProperty(
                "PoolPartyHarvester.defaultExportModule"));
        if (getMetadata) {
            exportModules.add("adms");
            exportModules.add("void");
        }

        logger.debug("Getting project from " + remoteUrl);

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(remoteUrl);
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

            String responseData = response.readEntity(String.class);

            String filePath = ToolkitFileUtils.saveFile(
                    outputPath,
                    exportModule,
                    format, responseData);
            if (returnOutputPaths) {
                results.put(exportModule, filePath);
            }
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
            TasksUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
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
