/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/** Response generation support for REST web services, when HTTP status 404
 * (Not Found) is required.
 * See <a
 * href="https://jersey.java.net/documentation/latest/representations.html"
 * >https://jersey.java.net/documentation/latest/representations.html</a>
 */
public class ResourceNotFoundException extends WebApplicationException {

    /** Generated UID for serialization. */
    private static final long serialVersionUID = -5804381045415391225L;

    /** Create a response representing HTTP status 404 (Not Found).
     * No body is returned.
     */
    public ResourceNotFoundException() {
        super(Response.status(Status.NOT_FOUND).build());
    }

    /** Create a response representing HTTP status 404 (Not Found).
     * @param message the String that will be sent back in the body
     *      of the response, as plain text.
     */
    public ResourceNotFoundException(final String message) {
        super(Response.status(Status.NOT_FOUND).entity(message)
                .type("text/plain").build());
    }

}
