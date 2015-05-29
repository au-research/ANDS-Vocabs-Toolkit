package au.org.ands.vocabs.toolkit.restlet;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskRunner;

/** Restlets for running a Toolkit supported tasks. */
@Path("runTask")
public class RunTask {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Injected servlet context. */
    @Context
    private ServletContext context;

    /** Get the list of PoolParty projects.
     * @param taskId The task id.
     * @return The list of PoolParty projects, in JSON format,
     * as returned by PoolParty. */
    @Path("{taskId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @GET
    public final HashMap<String, String> runTask(
            @PathParam("taskId") final int taskId) {
        logger.debug("called runTask, taskid = " + taskId);
        TaskInfo taskInfo = TasksUtils.getTaskInfo(taskId);
        TaskRunner runner = new TaskRunner(taskInfo);
        runner.runTask();
        return runner.getResults();
        // return "{\"hello\":\"Hello JSON!\"}";
    }

}
