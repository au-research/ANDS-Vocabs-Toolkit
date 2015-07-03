/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.restlet;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.provider.backup.PoolPartyBackupProvider;

/** Restlets for doing backups. */
@Path("doBackup")
public class Backup {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Backup a PoolParty project.
     * @param pPProjectId A PoolParty project ID.
     * @return The result info, in JSON format,
     * containing all files and some metadata. */
    @Path("PoolParty/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final HashMap<String, Object> backupPoolPartyProject(
            @PathParam("project_id")
            final String pPProjectId) {
        HashMap<String, Object> result =
                new HashMap<String, Object>();
        logger.debug("Called doBackup/PoolParty/ " + pPProjectId);
        result.putAll(new PoolPartyBackupProvider().backup(pPProjectId));
        return result;
    }

    /** Backup all PoolParty projects.
     * @return The result info, in JSON format,
     * containing all files and some metadata. */
    @Path("PoolParty")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final HashMap<String, Object> backupAllPoolPartyProjects() {
        HashMap<String, Object> result =
                new HashMap<String, Object>();
        logger.debug("Called doBackup/PoolParty/all");
        result.putAll(new PoolPartyBackupProvider().backup(null));
        return result;
    }
}
