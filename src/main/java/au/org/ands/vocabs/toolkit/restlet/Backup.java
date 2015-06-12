package au.org.ands.vocabs.toolkit.restlet;

//Disable LineLength check just for Eclipse-maintained imports.
//CHECKSTYLE:OFF: LineLength
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

import au.org.ands.vocabs.toolkit.provider.backup.PoolPartyBackupProvider;
//CHECKSTYLE:ON: LineLength

/** Restlets for getting vocabulary metadata. */
@Path("doBackup")
public class Backup {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Injected servlet context. */
    @Context
    private ServletContext context;

    /** Backup a PoolParty project.
     * @param pPProjectId PoolParty project id
     * @return the result info, in JSON format,
     * containing all files and some metadata. */
    @Path("poolParty/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final HashMap<String, Object> backupPoolPartyProject(
            @PathParam("project_id")
            final String pPProjectId) {
        HashMap<String, Object> result =
                new HashMap<String, Object>();
        logger.info("called getMetadata/poolParty " + pPProjectId);
        result.putAll(new PoolPartyBackupProvider().doBackup(pPProjectId));
        return result;
    }

    @Path("poolParty")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final HashMap<String, Object> backupAllPoolPartyProjects(

            final String pPProjectId) {
        HashMap<String, Object> result =
                new HashMap<String, Object>();
        logger.info("called getMetadata/poolParty " + pPProjectId);
        result.putAll(new PoolPartyBackupProvider().doBackup(null));
        return result;
    }
}
