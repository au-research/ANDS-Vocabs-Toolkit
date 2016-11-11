/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.backup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.PoolPartyUtils;
import au.org.ands.vocabs.toolkit.utils.PropertyConstants;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitNetUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;
import ch.qos.logback.classic.Level;

/** Backup provider for PoolParty. */
public class PoolPartyBackupProvider extends BackupProvider {

    /** The logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Get all PoolParty project IDs.
     * @return An ArrayList of all IDs as Strings.
     */
    public final ArrayList<String> getProjectIDs() {
        String remoteUrl = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_REMOTEURL);
        String username = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_USERNAME);
        String password = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_PASSWORD);

        logger.debug("Getting metadata from " + remoteUrl);

        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(remoteUrl)
                .path(PoolPartyUtils.API_PROJECTS);
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(username, password);
        target.register(feature);

        Invocation.Builder invocationBuilder =
                target.request(MediaType.APPLICATION_JSON);

        Response response = invocationBuilder.get();

        InputStream is = response.readEntity(InputStream.class);

        // Now extract the project IDs from the returned data and store
        // in the ArrayList that will be returned.
        JsonReader jsonReader = Json.createReader(is);
        JsonArray jsonStructure = jsonReader.readArray();
        ArrayList<String> pList = new ArrayList<String>();
        Iterator<JsonValue> iter = jsonStructure.iterator();
        while (iter.hasNext()) {
            JsonObject entry = (JsonObject) iter.next();
            pList.add(entry.getString("id"));
        }

        // Tidy up I/O resources.
        try {
            is.close();
        } catch (IOException e) {
            logger.error("Exception while closing InputStream", e);
        }
        response.close();
        return pList;
    }

    /** Do a backup of one PoolParty project. Return a list with the result
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
        String remoteUrl = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_REMOTEURL);
        String username = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_USERNAME);
        String password = ToolkitProperties.getProperty(
                PropertyConstants.POOLPARTY_PASSWORD);

        // Has to be either TriG or TriX.
        // We could do this as a Toolkit property (as we used to,
        // though incorrectly piggy-backing the PP harvester setting),
        // but there does not yet appear to be a gain.
        String format = "TriG";

        List<String> exportModules = new ArrayList<String>();

        // The following list of export modules comes from:
        // https://help.poolparty.biz/doc/developer-guide/
        //   basic-advanced-server-apis/poolparty-api-guide/
        //   general-remarks-concerning-poolparty-api/
        //   poolparty-project-modules
        // If the list on the web page changes, change the following ...
        exportModules.add("concepts");
        exportModules.add("workflow");
        exportModules.add("history");
        exportModules.add("suggestedConcepts");
        exportModules.add("void");
        exportModules.add("adms");
        exportModules.add("candidateConcepts");
        exportModules.add("lists");
        exportModules.add("deprecatedConcepts");
        exportModules.add("skosnotes");
        exportModules.add("linkedData");

        logger.debug("Getting project from " + remoteUrl);

        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(remoteUrl).
                path(PoolPartyUtils.API_PROJECTS);
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

        Invocation.Builder invocationBuilder =
                thisTarget.request(MediaType.APPLICATION_XML);

        Response response = invocationBuilder.get();

        if (response.getStatus()
                < Response.Status.BAD_REQUEST.getStatusCode()) {
            String responseData = response.readEntity(String.class);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
            String fileName = dateFormat.format(date) + "-backup";
            String filePath = ToolkitFileUtils.saveFile(
                    outputPath,
                    fileName,
                    format, responseData);
            result.put(fileName, filePath);
        } else {
            logger.error("getBackupFiles got an error from PoolParty; "
                    + "response code = " + response.getStatus());
            // This is an abuse of the task status codes, because
            // it is not a task.
            result.put(TaskStatus.ERROR, "getBackupFiles got an error "
                    + "from PoolParty; "
                    + "response code = " + response.getStatus());
        }

        // Tidy up I/O resources.
        response.close();

        return result;
    }

    /** Do a backup. Update the result parameter with the result
     * of the backup.
     * @param pPProjectId Either the PoolParty project ID, or null for all
     * projects.
     * @return the complete list of the backup files.
     */
    @Override
    public final HashMap<String, Object> backup(final String pPProjectId) {

        ArrayList<String> pList;
        HashMap<String, Object> results = new HashMap<String, Object>();

        if (pPProjectId == null || pPProjectId.isEmpty()) {
            pList = getProjectIDs();
        } else {
            pList = new ArrayList<String>();
            pList.add(pPProjectId);
        }

        for (String projectId : pList) {
            try {
                ToolkitFileUtils.compressBackupFolder(projectId);
            } catch (IOException ex) {
                results.put(TaskStatus.EXCEPTION, "Unable to compress folder"
                        + " for projectId:" + projectId);
                logger.error("Unable to compress folder", ex);
            }
            results.put(projectId, getBackupFiles(projectId,
                    ToolkitFileUtils.getBackupPath(projectId)));
        }

        return results;
    }

    /**
     * Main method to allow running backups from the command line.
     * @param args Command-line arguments.
     * If the first argument is "-d", enable debugging; otherwise,
     * debugging output is disabled. Then either specify one
     * Poolparty project ID, or specify no additional parameters to backup
     * all projects.
     */
    public static void main(final String[] args) {
        // The value specified as a parameter to getLogger() must be
        // specific enough to cover any settings in logback.xml.
        // The casting is done to enable the subsequent call to setLevel().
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger)
                org.slf4j.LoggerFactory.getLogger(
                        "au.org.ands.vocabs");
        // Put command-line arguments into an ArrayList to make them
        // easier to process. (I.e., as one would use "shift" in
        // Bourne shell.)
        ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
        if (argsList.size() > 0 && "-d".equals(argsList.get(0))) {
            rootLogger.setLevel(Level.DEBUG);
            argsList.remove(0);
        } else {
            rootLogger.setLevel(Level.INFO);
        }

        switch (argsList.size()) {
        case 0:
            new PoolPartyBackupProvider().backup(null);
            break;
        case 1:
            new PoolPartyBackupProvider().backup(argsList.get(0));
            break;
        default:
            System.err.println("Wrong number of arguments.");
            System.exit(1);
        }
    }

}
