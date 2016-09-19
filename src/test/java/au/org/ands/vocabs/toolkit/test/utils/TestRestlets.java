/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.test.utils;

import java.lang.invoke.MethodHandles;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.test.arquillian.ArquillianTestUtils;

/** Restlets that support testing. */
@Path("testing")
public class TestRestlets {

    /** Servlet context. */
    @Context
    private ServletContext context;

    /** Clear the database.
     * @throws Exception If there is a problem with the database.
     * @return The string "OK".
     */
    @Path("clearDB")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final String clearDB() throws Exception {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("Clearing the database.");
        ArquillianTestUtils.clearDatabase();
        return "OK";
    }

}
