/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.utils;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.function.Function;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.utils.ToolkitNetUtils;

/** Utility methods for testing the toolkit as a remote client. */
public final class NetClientUtils {

    /** Logger for this class. */
    private static Logger logger;

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

    /** Private constructor for a utility class. */
    private NetClientUtils() {
    }

    /** Perform a GET request of a remote server.
     * Redirects are followed.
     * No MediaType is specified in the request.
     * @param baseURL The base URL of the server.
     * @param path The path to the request; appended to {@code baseURL}.
     * @return The response from the GET request. It is the responsibility
     * of the caller to invoke the {@code close()} method on the response.
     */
    public static Response doGet(final URL baseURL,
            final String path) {
        logger.info("doGet: baseURL = " + baseURL + "; path = " + path);
        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(baseURL.toString()).
                path(path);
        Response response = target.request().get();
        return response;
    }

    /** Perform a GET request of a remote server.
     * Redirects are followed.
     * @param baseURL The base URL of the server.
     * @param path The path to the request; appended to {@code baseURL}.
     * @param responseMediaType The MediaType to be requested of the server.
     * @return The response from the GET request. It is the responsibility
     * of the caller to invoke the {@code close()} method on the response.
     */
    public static Response doGet(final URL baseURL,
            final String path, final MediaType responseMediaType) {
        logger.info("doGet: baseURL = " + baseURL + "; path = " + path
                + "; responseMediaType = " + responseMediaType);
        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(baseURL.toString()).
                path(path);
        Response response =
                target.request(responseMediaType).get();
        return response;
    }

    /** Perform a GET request of a remote server.
     * Redirects are followed.
     * Additional components are applied to the WebTarget before it
     * is used. These components can be, for example, adding
     * query parameters. No MediaType is
     * specified in the request.
     * @param baseURL The base URL of the server.
     * @param path The path to the request; appended to {@code baseURL}.
     * @param additionalComponents The additional operations applied to
     *      the WebTarget, before it is used.
     * @return The response from the GET request. It is the responsibility
     * of the caller to invoke the {@code close()} method on the response.
     */
    public static Response doGetWithAdditionalComponents(final URL baseURL,
            final String path,
            final Function<WebTarget, WebTarget> additionalComponents) {
        logger.info("doGetWithAdditionalComponents: baseURL = " + baseURL
                + "; path = " + path);
        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(baseURL.toString()).path(path);
        target = additionalComponents.apply(target);
        Response response = target.request().get();
        return response;
    }

    /** Perform a GET request of a remote server.
     * Do not follow redirects.
     * Additional components are applied to the WebTarget before it
     * is used. These components can be, for example, adding
     * query parameters. No MediaType is
     * specified in the request.
     * @param baseURL The base URL of the server.
     * @param path The path to the request; appended to {@code baseURL}.
     * @param additionalComponents The additional operations applied to
     *      the WebTarget, before it is used.
     * @return The response from the GET request. It is the responsibility
     * of the caller to invoke the {@code close()} method on the response.
     */
    public static Response doGetWithAdditionalComponentsNoRedirects(
            final URL baseURL,
            final String path,
            final Function<WebTarget, WebTarget> additionalComponents) {
        logger.info("doGetWithAdditionalComponentsNoRedirects: baseURL = "
            + baseURL + "; path = " + path);
        Client client = ToolkitNetUtils.getClientNoRedirects();
        WebTarget target = client.target(baseURL.toString()).path(path);
        target = additionalComponents.apply(target);
        Response response = target.request().get();
        return response;
    }

}
