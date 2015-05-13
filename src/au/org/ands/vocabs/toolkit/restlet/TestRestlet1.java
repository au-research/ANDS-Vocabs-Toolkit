package au.org.ands.vocabs.toolkit.restlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/** Testing restlet. */
@Path("testing")
public class TestRestlet1 {

    /** getMessage.
     * @return the message. */
    @Path("test.text")
    @Produces("text/plain")
    @GET
    public final String getMessage() {
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
