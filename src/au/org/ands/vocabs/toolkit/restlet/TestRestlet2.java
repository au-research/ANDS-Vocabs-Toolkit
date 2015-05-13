package au.org.ands.vocabs.toolkit.restlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/** Testing restlet. */
@Path("testing2")
public class TestRestlet2 {

    /** getMessage.
     * @return the message. */
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final String getMessage() {
        return "Hello World! Again!";
        }

    /** getMessage.
     * @return the message. */
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final String getMessageJson() {
        return "{\"hello\":\"Hello JSON!\"}";
        }

}
