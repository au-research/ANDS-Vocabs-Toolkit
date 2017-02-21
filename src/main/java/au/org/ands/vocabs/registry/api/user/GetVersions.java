/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.api.user;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.registry.db.converter.VersionDbSchemaMapper;
import au.org.ands.vocabs.registry.db.dao.VersionDAO;
import au.org.ands.vocabs.registry.schema.vocabulary201701.Version;

/** REST web services for getting versions. */
@Path("/api/resource")
public class GetVersions {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Get the current instance of a version, by its version id.
     * @param versionId The VersionId of the version to be fetched.
     * @return The version, in either XML or JSON format,
     *      or an error result, if there is no such version. */
    @Path("version/{versionId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @GET
    public final Response getVersionById(
            @PathParam("versionId") final Integer versionId) {
        logger.debug("called getVersion: " + versionId);
        au.org.ands.vocabs.registry.db.entity.Version
            dbVersion = VersionDAO.getCurrentVersionByVersionId(
                    versionId);
        Version outputVersion;

        VersionDbSchemaMapper mapper =
                VersionDbSchemaMapper.INSTANCE;
        outputVersion = mapper.sourceToTarget(dbVersion);
        if (outputVersion == null) {
            return Response.status(Status.BAD_REQUEST).entity(
                    new ErrorResult("No version with that id")).build();
        }
        return Response.ok().entity(outputVersion).build();
    }



}
