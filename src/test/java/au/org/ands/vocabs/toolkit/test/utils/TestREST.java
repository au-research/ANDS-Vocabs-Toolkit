/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.test.utils;

import java.lang.invoke.MethodHandles;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.test.arquillian.ArquillianTestUtils;

/** REST web services that support testing. */
@Path("testing")
public class TestREST {

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

    /** Load data into the database.
     * @param testName The name of the test method. Used to generate
     *      the filename of the file to load.
     * @throws Exception If there is a problem with the database.
     * @return The string "OK".
     */
    @Path("loadDB")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final String loadDB(@QueryParam("testName") final String testName)
            throws Exception {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("Loading the database.");
        ArquillianTestUtils.loadDbUnitTestFile(testName);
        return "OK";
    }

    /** Load data into the database as an update.
     * @param testName The name of the test method. Used to generate
     *      the path to the file to load.
     * @param filename The name of the file to be loaded.
     * @throws Exception If there is a problem with the database.
     * @return The string "OK".
     */
    @Path("loadDBAsUpdate")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final String loadDBAsUpdate(
            @QueryParam("testName") final String testName,
            @QueryParam("filename") final String filename)
            throws Exception {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("Loading the database as an update.");
        ArquillianTestUtils.loadDbUnitTestFileAsUpdate(testName, filename);
        return "OK";
    }

}
