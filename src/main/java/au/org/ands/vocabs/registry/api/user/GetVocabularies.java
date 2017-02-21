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

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.registry.db.converter.VersionDbSchemaMapper;
import au.org.ands.vocabs.registry.db.converter.VocabularyDbSchemaMapper;
import au.org.ands.vocabs.registry.db.dao.VersionDAO;
import au.org.ands.vocabs.registry.db.dao.VocabularyDAO;
import au.org.ands.vocabs.registry.schema.vocabulary201701.Version;
import au.org.ands.vocabs.registry.schema.vocabulary201701.Vocabulary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/** REST web services for getting vocabularies. */
@Path("/api/resource")
@Api
public class GetVocabularies {

    // For my info: if adding a method with return type Response
    // where the value returned is a list, wrap it in a GenericEntity.
    // See http://www.adam-bien.com/roller/abien/entry/jax_rs_returning_a_list

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Get all the current vocabularies.
     * @return The list of vocabularies, in either XML or JSON format. */
    @Path("vocabulary")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "Get all the current vocabularies.")
    public final List<Vocabulary> getVocabularies() {
        logger.debug("called getVocabularies");
        List<au.org.ands.vocabs.registry.db.entity.Vocabulary>
            dbVocabularies = VocabularyDAO.getAllCurrentVocabulary();
        List<Vocabulary> outputVocabularies = new ArrayList<>();

        VocabularyDbSchemaMapper mapper =
                VocabularyDbSchemaMapper.INSTANCE;
        for (au.org.ands.vocabs.registry.db.entity.Vocabulary dbVocabulary
                : dbVocabularies) {
            outputVocabularies.add(mapper.sourceToTarget(dbVocabulary));
        }

        return outputVocabularies;
    }

    /** Get the current instance of a vocabulary, by its vocabulary id.
     * @param vocabularyId The VocabularyId of the vocabulary to be fetched.
     * @return The vocabulary, in either XML or JSON format,
     *      or an error result, if there is no such vocabulary. */
    @Path("vocabulary/{vocabularyId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "Get a current vocabulary by its id.",
            response = Vocabulary.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST,
                    message = "No vocabulary with that id",
                    response = ErrorResult.class)
            })
    public final Response getVocabularyById(
            @PathParam("vocabularyId") final Integer vocabularyId) {
        logger.debug("called getVocabulary: " + vocabularyId);
        au.org.ands.vocabs.registry.db.entity.Vocabulary
            dbVocabulary = VocabularyDAO.getCurrentVocabularyByVocabularyId(
                    vocabularyId);
        Vocabulary outputVocabulary;

        VocabularyDbSchemaMapper mapper =
                VocabularyDbSchemaMapper.INSTANCE;
        outputVocabulary = mapper.sourceToTarget(dbVocabulary);
        if (outputVocabulary == null) {
            return Response.status(Status.BAD_REQUEST).entity(
                    new ErrorResult("No vocabulary with that id")).build();
        }
        return Response.ok().entity(outputVocabulary).build();
    }

    /** Get the current versions of a vocabulary, by its vocabulary id.
     * @param vocabularyId The VocabularyId of the versions to be fetched.
     * @return The list of versions, in either XML or JSON format,
     *      or an error result, if there is no such vocabulary. */
    @Path("vocabulary/{vocabularyId}/version")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @GET
    @ApiOperation(value = "Get the current versions of a vocabulary, "
            + "by its vocabulary id.")
    public final List<Version> getVersionsForVocabularyById(
            @PathParam("vocabularyId") final Integer vocabularyId) {
        logger.debug("called getVersionsForVocabularyById: " + vocabularyId);

        List<au.org.ands.vocabs.registry.db.entity.Version>
            dbVersions = VersionDAO.getCurrentVersionListForVocabulary(
                    vocabularyId);
        List<Version> outputVersions = new ArrayList<>();

        VersionDbSchemaMapper mapper =
                VersionDbSchemaMapper.INSTANCE;
        for (au.org.ands.vocabs.registry.db.entity.Version dbVersion
                : dbVersions) {
            outputVersions.add(mapper.sourceToTarget(dbVersion));
        }

        return outputVersions;
    }



}
