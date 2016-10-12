/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.rest;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.ResourceMapEntryUtils;
import au.org.ands.vocabs.toolkit.db.model.ResourceMapEntry;

/** REST web services for resolution of concept IRIs. */
@Path("resolve")
public class ResolveIRI {

    /** Error message to return, if no IRI is specified as a query
    * parameter in the request URL. */
    public static final String NO_IRI_SPECIFIED =
            "No IRI specified as query parameter.";

    /** Error message to return, if more than one vocabulary defines
     * the resource. */
    public static final String MULTIPLE_DEFINITIONS =
            "Can't resolve: more than one vocabulary defines the resource";

    /** Error message to return, if an unsupported lookup mode is
    * requested. */
   public static final String UNSUPPORTED_MODE = "Unsupported mode";

   /** Error message to return, if no vocabulary defines
     * the resource. */
   public static final String NO_DEFINITION =
           "Can't resolve: no vocabulary defines the resource";

    /** Error message to return, if what would be sent back to the
     * client is found to be not a valid URL. */
    public static final String CAN_NOT_REDIRECT =
            "Can't redirect: the result is not a valid URL";

    /** Logger for this class. */

    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Resolve an IRI against the global map of resources.
     * An idea for possible future support: query parameter code=xyz,
     * to specify the return status code (other than the default,
     * provided by {@link Response#temporaryRedirect(URI)}, which is 307).
     * @return The list of PoolParty projects, in JSON format,
     * as returned by PoolParty.
     * @param mode The mode of resolution. For now, only "current"
     *      is supported, and this is the default. Specified as a query
     *      parameter.
     * @param iri The IRI to be resolved. This is a required query parameter.
     * @param suffix An optional suffix to be appended to the redirected URL.
     *      Specified as a query parameter.
     */
    @Path("lookupIRI")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final Response lookupIRI(
            @QueryParam("mode") final String mode,
            @QueryParam("iri") final String iri,
            @QueryParam("suffix") final String suffix) {
        logger.debug("called lookupIRI with mode=" + mode
                + ", iri=" + iri
                + ", suffix=" + suffix);
        if (iri == null) {
            throw new ResourceNotFoundException(NO_IRI_SPECIFIED);
        }
        // Check the optional mode query parameter. For now, only
        // "current" is supported.
        if (mode != null && !"current".equals(mode)) {
            throw new ResourceNotFoundException(UNSUPPORTED_MODE
                    + ": " + mode);
        }
        // Check the optional suffix query parameter. Need this bit
        // of logic to generate suffixToAdd, rather than simply using
        // suffix itself, since null converted to a string is "null",
        // not the empty string!
        String suffixToAdd = "";
        if (suffix != null) {
            suffixToAdd = suffix;
        }
        List<ResourceMapEntry> resourceMapEntries =
                ResourceMapEntryUtils.
                    getCurrentOwnedResourceMapEntriesForIRI(iri);
        if (resourceMapEntries.size() == 0) {
            throw new ResourceNotFoundException(NO_DEFINITION
                    + ": " + iri);
        }
        // It could be that there are multiple definitions of the
        // same resource within the same access point. That is
        // probably a mistake in the vocabulary, but we can
        // still resolve the resource.
        // So, look at the first access point in the results, and
        // see if any of the other returned access points are different.
        ResourceMapEntry firstResourceMapEntry = resourceMapEntries.get(0);
        int accessPointId = firstResourceMapEntry.getAccessPointId();
        for (int i = 1; i < resourceMapEntries.size(); i++) {
            if (resourceMapEntries.get(i).getAccessPointId()
                    != accessPointId) {
                // Found a different access point ID.
                throw new ResourceNotFoundException(
                        MULTIPLE_DEFINITIONS
                        + ": " + iri);
            }
        }
        // If we reached this point, all resource map entries
        // returned have the same access point ID.
        // We just use the first one returned.
        String redirect =
                ResourceMapEntryUtils.getRedirectForResourceMapEntry(
                firstResourceMapEntry) + suffixToAdd;
        try {
            URI redirectURI = new URI(redirect);
            return Response.temporaryRedirect(redirectURI).build();
            // If supporting codes other than 307, check that
            // the code specified is one of 301, 302, 303, 307, and
            // then use something like:
            // return Response.status(code).location(redirectURI).build();
        } catch (URISyntaxException e) {
            logger.error("Unable to create redirection URI", e);
            throw new ResourceNotFoundException(
                    CAN_NOT_REDIRECT + ": " + redirect);
        }
    }

}
