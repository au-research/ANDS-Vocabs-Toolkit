package au.org.ands.vocabs.toolkit.provider.backup;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
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
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

/** Harvest provider for PoolParty. */
public class PoolPartyBackupProvider extends BackupProvider {

    /** The logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    @Override
    /** get All PoolParty project IDs
     * of the backup.
     * @return a ArrayList of all IDs as String.
     */
    public final ArrayList<String> getProjects() {
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

        InputStream is = response.readEntity(InputStream.class);
        JsonReader jsonReader = Json.createReader(is);
        JsonArray jsonStructure = jsonReader.readArray();
        logger.debug("jsonStructure size  " + jsonStructure.size());
        ArrayList<String> pList = new ArrayList<String>();
        Iterator<JsonValue> iter = jsonStructure.iterator();
        while (iter.hasNext()) {
            JsonObject entry = (JsonObject) iter.next();
            logger.debug("jsonStructure size  " + entry.toString());
            pList.add(entry.getString("id"));
        }
        return pList;

    }

    /** Do a backup. Return a list with the result
     * of the backup.
     * @param ppProjectId The PoolParty project id.
     * @param outputPath The directory in which to store output files.
     * of each harvested file in the results map.
     * @return results HashMap representing the result of the backup.
     */
    public final HashMap<String, String> getBackupFiles(
            final String ppProjectId,
            final String outputPath) {
        HashMap<String, String> result = new HashMap<String, String>();
        String remoteUrl = PROPS.getProperty("PoolPartyHarvester.remoteUrl");
        String username = PROPS.getProperty("PoolPartyHarvester.username");
        String password = PROPS.getProperty("PoolPartyHarvester.password");

        String format = PROPS.getProperty("PoolPartyHarvester.defaultFormat");

        List<String> exportModules = new ArrayList<String>();

        exportModules.add("concepts");
        exportModules.add("workflow");
        exportModules.add("history");
        exportModules.add("freeConcepts");
        exportModules.add("void");
        exportModules.add("adms");
        exportModules.add("void");

        logger.debug("Getting project from " + remoteUrl);

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(remoteUrl);
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(username, password);
        WebTarget thisTarget = target.register(feature)
                .path(ppProjectId)
                .path("export")
                .queryParam("format", format);
            for (String exportModule : exportModules) {
               thisTarget = thisTarget.queryParam("exportModules",
                        exportModule);
            }
            logger.debug("Harvesting from " + thisTarget.toString());

            Invocation.Builder invocationBuilder =
                    thisTarget.request(MediaType.APPLICATION_XML);

            Response response = invocationBuilder.get();

            String responseData = response.readEntity(String.class);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String filaName = dateFormat.format(date) + "-backup";
            String filePath = ToolkitFileUtils.saveFile(
                    outputPath,
                    filaName,
                    format, responseData);
           result.put(filaName, filePath);

        return result;
    }

    /** Do a backup. Update the result parameter with the result
     * of the harvest.
     * @param pPProjectId (optional) the poolParty project id or null for all
     * projects.
     * @return the complete list of the backup files.
     */
    @Override
    public final HashMap<String, Object> doBackup(final String pPProjectId) {

        ArrayList<String> pList = new ArrayList<String>();
        HashMap<String, Object> results = new HashMap<String, Object>();

        if (pPProjectId == null || pPProjectId.isEmpty()) {
            pList = getProjects();
        } else {
            pList.add(pPProjectId);
        }

        for (int i = 0; i < pList.size(); i++) {
            String projectId = pList.get(i);
            results.put(projectId, getBackupFiles(projectId,
                    TasksUtils.getMetadataBackupPath(projectId)));
        }

        return results;
    }


}
