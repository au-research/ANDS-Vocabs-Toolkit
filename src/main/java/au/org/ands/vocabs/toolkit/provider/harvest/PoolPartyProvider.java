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

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;

/** Provider for PoolParty. */
public class PoolPartyProvider extends HarvestProvider {

    /** The logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());
//    private UriInfo info;

    @Override
    public String getInfo() {
        String remoteUrl = props.getProperty("PoolPartyHarvester.remoteUrl");
        String username = props.getProperty("PoolPartyHarvester.username");
        String password = props.getProperty("PoolPartyHarvester.password");

        logger.debug("Getting metadata from "+ remoteUrl);

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
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    @Override
    public boolean harvest(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        String remoteUrl = props.getProperty("PoolPartyHarvester.remoteUrl");
        String username = props.getProperty("PoolPartyHarvester.username");
        String password = props.getProperty("PoolPartyHarvester.password");
        String projectId = taskInfo.getVocabulary().getPoolPartyId();

        String format = props.getProperty("PoolPartyHarvester.defaultFormat");

        String basicAuth ="";

        // Possible future work: support specifying particular modules.
//        List<String> exportModules =
//                info.getQueryParameters().get("exportModules");
        List<String> exportModules = null;

        if (exportModules == null) {
            exportModules = new ArrayList<String>();
        }
        if (exportModules.isEmpty()) {
            exportModules.add(props.getProperty(
                    "PoolPartyHarvester.defaultExportModule"));
        }

        logger.debug("Getting project from "+ remoteUrl);

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(remoteUrl);
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(username, password);
        WebTarget plainTarget = target.register(feature)
                .path(projectId)
                .path("export")
                .queryParam("format", format);

        for (String exportModule : exportModules) {
            results.put("remoteUrl", remoteUrl);
            WebTarget thisTarget = plainTarget.queryParam("exportModules",
                    exportModule);

            logger.info("Harvesting from "+ thisTarget.toString());

            Invocation.Builder invocationBuilder =
                    thisTarget.request(MediaType.APPLICATION_XML);

            Response response = invocationBuilder.get();

            String responseData = response.readEntity(String.class);

//            InputStream is = getInputStream(requestUrl, basicAuth);

//            if (exportModule.equals("concepts")) {
//
//                message.add("concepts_tree", createSolrJson(RDFFormat.RDFXML, is, projectId));
//            }
//            is = getInputStream(requestUrl, basicAuth);
//            String data = getFragment(is);
            results.put(exportModule, saveFile(projectId, exportModule, format, responseData));
        }

        return true;
    }


}
