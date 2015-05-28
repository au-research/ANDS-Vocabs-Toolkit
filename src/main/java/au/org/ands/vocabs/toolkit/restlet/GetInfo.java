package au.org.ands.vocabs.toolkit.restlet;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.provider.harvest.HarvestProviderUtils;

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
            // TODO Auto-generated catch block
            logger.error("Exception happened: ", e);
            // e.printStackTrace();
            return "{\"exception\":\"Can't get PoolParty provider.\"}";
        }
    }

    /** Get a complete list of tasks.
     * @return The list of tasks. */
    @Path("systemHealthCheck")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final List<Task> systemHealthCheck() {
        logger.debug("called systemHealthCheck");
        List<Task> tasks = TasksUtils.getAllTasks();
        return tasks;
    }

}
