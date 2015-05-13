package au.org.ands.vocabs.toolkit.restlet;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Testing restlet. */
@Path("testing")
public class TestRestlet1 {

    /** getMessage.
     * @return the message. */
    @Path("test.text")
    @Produces("text/plain")
    @GET
    public final String getMessage() {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("Running TestRestlet1.getMessage().");

        return "Hello World! Again!";
        }

    /** getMessage.
     * @return the message. */
    @Path("test.json")
    @Produces("application/json")
    @GET
    public final String getMessageJson() {
        return "{\"hello\":\"Hello JSON!\"}";
        }

}
