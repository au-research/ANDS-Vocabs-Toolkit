/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.restlet;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.provider.harvest.HarvestProviderUtils;
import au.org.ands.vocabs.toolkit.provider.importer.ImporterProviderUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Restlets for getting info about Toolkit supported services. */
@Path("getInfo")
public class GetInfo {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Injected servlet context. */
    @Context
    private ServletContext context;

    /** Get the list of PoolParty projects.
     * @return The list of PoolParty projects, in JSON format,
     * as returned by PoolParty. */
    @Path("PoolPartyProjects")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final String getInfoPoolParty() {
        logger.debug("called getInfoPoolParty");
        try {
            return HarvestProviderUtils.getProvider("PoolParty").getInfo();
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            logger.error("Exception happened: ", e);
            return "{\"exception\":\"Can't get PoolParty provider.\"}";
        }
    }

    /** Get the list of Sesame repositories.
     * @return The list of repositories, in JSON format. */
    @Path("SesameRepositories")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final Collection<?> getInfoSesame() {
        logger.debug("called getInfoSesame");
        try {
            return ImporterProviderUtils.getProvider("Sesame").getInfo();
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            logger.error("Exception happened: ", e);
            // e.printStackTrace();
//            return "{\"exception\":\"Can't get Sesame provider.\"}";
            return new ArrayList<String>();
        }
    }

    /** Get a complete list of tasks.
     * @return The list of tasks. */
    @Path("systemHealthCheck")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final List<Task> systemHealthCheck() {
        logger.debug("called systemHealthCheck");
        List<Task> tasks = TaskUtils.getAllTasks();
        return tasks;
    }

    /** Get the Toolkit version information from the version.properties
     * configuration file.
     * @return The version information.
     */
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final HashMap<String, String> getVersion() {
        logger.debug("called getVersion");
        Properties props = ToolkitProperties.getProperties();
        HashMap<String, String> result =
                new HashMap<String, String>();
        result.put("Toolkit.version", props.getProperty("Toolkit.version"));
        result.put("Toolkit.versionTimestamp",
                props.getProperty("Toolkit.versionTimestamp"));
        result.put("Toolkit.buildDate",
                props.getProperty("Toolkit.buildDate"));
        return result;
    }


}
