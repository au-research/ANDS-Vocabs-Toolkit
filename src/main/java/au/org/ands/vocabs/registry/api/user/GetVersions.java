/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.api.user;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.registry.db.converter.AccessPointDbSchemaMapper;
import au.org.ands.vocabs.registry.db.converter.VersionDbSchemaMapper;
import au.org.ands.vocabs.registry.db.dao.AccessPointDAO;
import au.org.ands.vocabs.registry.db.dao.VersionDAO;
import au.org.ands.vocabs.registry.schema.vocabulary201701.AccessPoint;
import au.org.ands.vocabs.registry.schema.vocabulary201701.Version;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/** REST web services for getting versions. */
@Path("/api/resource")
@Api
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
    @ApiOperation(value = "Get a current version by its id.",
            response = Version.class)
    public final Response getVersionById(
            @ApiParam(value = "The ID of the version to get")
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

    /** Determine if a version has a draft instance.
     * @param versionId The VersionId of the vocabulary to be checked.
     * @return True, if the version has a draft instance. False,
     *      if there is no draft instance with that version id.*/
    @Path("version/{versionId}/hasDraft")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "Determine if a version has a draft instance.",
            notes = "Returns true, if the version has a draft instance. "
            + "Returns false, if there is no draft instance with that "
            + "version id. The result is returned in the booleanValue "
            + "property.",
            response = SimpleResult.class)
    public final Response hasDraftVersionById(
            @ApiParam(value = "The ID of the version to check")
            @PathParam("versionId") final Integer versionId) {
        logger.debug("called hasDraftVersionById: " + versionId);
        boolean hasDraft = VersionDAO.hasDraftVersion(versionId);
        return Response.ok().entity(new SimpleResult(hasDraft)).build();
    }

    /** Get the current access points of a version, by its version id.
     * @param versionId The VersionId of the access points to be fetched.
     * @return The list of access points, in either XML or JSON format,
     *      or an error result, if there is no such version. */
    @Path("version/{versionId}/accessPoint")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "Get the current access points of a version, "
            + "by its version id.")
    public final List<AccessPoint> getAccessPointsForVersionById(
            @ApiParam(value = "The ID of the version from which to get "
                    + "the current access points")
            @PathParam("versionId") final Integer versionId) {
        logger.debug("called getAccessPointsForVersionById: " + versionId);

        List<au.org.ands.vocabs.registry.db.entity.AccessPoint>
        dbAPs = AccessPointDAO.getCurrentAccessPointListForVersion(
                versionId);
        List<AccessPoint> outputAPs = new ArrayList<>();

        AccessPointDbSchemaMapper mapper =
                AccessPointDbSchemaMapper.INSTANCE;
        for (au.org.ands.vocabs.registry.db.entity.AccessPoint dbAP
                : dbAPs) {
            outputAPs.add(mapper.sourceToTarget(dbAP));
        }

        return outputAPs;
    }



}
