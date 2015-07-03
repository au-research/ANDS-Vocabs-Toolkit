/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.restlet;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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

    /** getMessage.
     * @return the message. */
    @Path("JsonObject")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final JsonObject getMessageJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        JsonObjectBuilder job2 = Json.createObjectBuilder();
        job.add("hello", "Hello JSON from ObjectBuilder");
        job2.add("nested2", "at the bottom");
        job.add("nested1", job2.build());
        return job.build();
    }

    /** getException.
     * This shows how to return a status code 503 "service unavailable"
     * and also some content.
     * @return the message. */
    @Path("except")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final Response getExcept() {
        return Response.status(Status.SERVICE_UNAVAILABLE).
                entity("Hi there").build();
    }

    /** getExceptJSON.
     * This shows how to return a status code 503 "service unavailable"
     * and also some JSON content.
     * @return the message. */
    @Path("except")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final Response getExceptJSON() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        JsonObjectBuilder job2 = Json.createObjectBuilder();
        job.add("hello", "Hello JSON from getExceptJSON ObjectBuilder");
        job2.add("nested2", "at the bottom");
        job.add("nested1", job2.build());
        return Response.status(Status.SERVICE_UNAVAILABLE).
                entity(job.build()).build();
    }



}
